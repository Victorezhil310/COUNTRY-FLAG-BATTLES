Add-Type -AssemblyName System.Drawing

$res = 512
$bmp = New-Object System.Drawing.Bitmap($res, $res)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

# ---- background: radial cosmic gradient ----
$rect = New-Object System.Drawing.Rectangle(0, 0, $res, $res)
$path = New-Object System.Drawing.Drawing2D.GraphicsPath
$path.AddEllipse(-160, -160, ($res + 320), ($res + 320))
$pgb = New-Object System.Drawing.Drawing2D.PathGradientBrush($path)
$pgb.CenterPoint = New-Object System.Drawing.PointF(($res / 2), ($res * 0.42))
$pgb.CenterColor = [System.Drawing.Color]::FromArgb(255, 86, 30, 176)
$pgb.SurroundColors = @([System.Drawing.Color]::FromArgb(255, 12, 3, 42))
$g.FillRectangle($pgb, $rect)

# ---- stars ----
$rnd = New-Object System.Random(7)
for ($i = 0; $i -lt 150; $i++) {
    $x = $rnd.Next(0, $res); $y = $rnd.Next(0, $res); $r = $rnd.Next(1, 4)
    $a = $rnd.Next(50, 190)
    $b = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb($a, 255, 255, 255))
    $g.FillEllipse($b, ($x - $r), ($y - $r), ($r * 2), ($r * 2))
    $b.Dispose()
}

$cx = $res / 2.0
$cy = $res / 2.0
$R = 150.0
$gapHalf = 25.0            # degrees of open gap, centred on the top of the ring
$start = -90.0 + $gapHalf
$sweep = 360.0 - 2 * $gapHalf

function Ring-Rect {
    param($radius)
    New-Object System.Drawing.RectangleF(($cx - $radius), ($cy - $radius), ($radius * 2), ($radius * 2))
}

# ---- ring: glow, coloured edge, white core ----
$glow = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(45, 224, 139, 255), 46)
$glow.StartCap = 'Round'; $glow.EndCap = 'Round'
$g.DrawArc($glow, (Ring-Rect $R), $start, $sweep)
$glow.Dispose()

$outer = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 192, 140, 255), 30)
$outer.StartCap = 'Round'; $outer.EndCap = 'Round'
$g.DrawArc($outer, (Ring-Rect $R), $start, $sweep)
$outer.Dispose()

$mid = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 40, 12, 90), 22)
$mid.StartCap = 'Round'; $mid.EndCap = 'Round'
$g.DrawArc($mid, (Ring-Rect $R), $start, $sweep)
$mid.Dispose()

$core = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 255, 255, 255), 11)
$core.StartCap = 'Round'; $core.EndCap = 'Round'
$g.DrawArc($core, (Ring-Rect $R), $start, $sweep)
$core.Dispose()

# ---- gap endpoint pips ----
foreach ($side in @(-1, 1)) {
    $deg = -90.0 + $side * $gapHalf
    $x = $cx + $R * [Math]::Cos($deg * [Math]::PI / 180.0)
    $y = $cy + $R * [Math]::Sin($deg * [Math]::PI / 180.0)
    $b = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 255, 226, 95))
    $g.FillEllipse($b, ($x - 13), ($y - 13), 26, 26)
    $b.Dispose()
}

# ---- flags: a few still inside the arena ----
$palette = @(
    [System.Drawing.Color]::FromArgb(255, 255, 61, 110),
    [System.Drawing.Color]::FromArgb(255, 255, 210, 63),
    [System.Drawing.Color]::FromArgb(255, 43, 255, 228),
    [System.Drawing.Color]::FromArgb(255, 124, 92, 255),
    [System.Drawing.Color]::FromArgb(255, 91, 255, 122),
    [System.Drawing.Color]::FromArgb(255, 255, 154, 60)
)

function Draw-Flag {
    param($gr, $x, $y, $w, $h, $deg, $color)
    $state = $gr.Save()
    $gr.TranslateTransform($x, $y)
    $gr.RotateTransform($deg)
    $dark = [System.Drawing.Color]::FromArgb(200, [Math]::Max(0, ($color.R - 150)), [Math]::Max(0, ($color.G - 150)), [Math]::Max(0, ($color.B - 150)))
    $shadow = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(90, 0, 0, 0))
    $gr.FillRectangle($shadow, (-$w / 2 + 3), (-$h / 2 + 4), $w, $h)
    $shadow.Dispose()
    $b1 = New-Object System.Drawing.SolidBrush($color)
    $gr.FillRectangle($b1, (-$w / 2), (-$h / 2), $w, $h)
    $b1.Dispose()
    $b2 = New-Object System.Drawing.SolidBrush($dark)
    $gr.FillRectangle($b2, (-$w / 2), 0, $w, ($h / 3))
    $b2.Dispose()
    $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(160, 255, 255, 255), 2)
    $gr.DrawRectangle($pen, (-$w / 2), (-$h / 2), $w, $h)
    $pen.Dispose()
    $gr.Restore($state)
}

$inside = @(
    @(200, 300, 18), @(312, 292, -14), @(256, 336, 6),
    @(196, 236, -8), @(322, 232, 12), @(256, 268, 0), @(222, 358, 20)
)
for ($i = 0; $i -lt $inside.Length; $i++) {
    $p = $inside[$i]
    Draw-Flag $g $p[0] $p[1] 42 28 $p[2] $palette[$i % $palette.Length]
}

# ---- flags escaping through the top gap, with motion streaks ----
$escaping = @(
    @(-38, 96, 30, 0), @(-14, 132, -22, 1), @(16, 78, 18, 2), @(40, 122, -30, 3), @(2, 158, 4, 4)
)
foreach ($e in $escaping) {
    $deg = -90.0 + $e[0]
    $rad = $deg * [Math]::PI / 180.0
    $dist = $R + $e[1]
    $x = $cx + [Math]::Cos($rad) * $dist
    $y = $cy + [Math]::Sin($rad) * $dist
    $pen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(70, 255, 255, 255), 3)
    $g.DrawLine($pen, ($cx + [Math]::Cos($rad) * $R), ($cy + [Math]::Sin($rad) * $R), ($x - [Math]::Cos($rad) * 14), ($y - [Math]::Sin($rad) * 14))
    $pen.Dispose()
    Draw-Flag $g $x $y 40 26 $e[2] $palette[$e[3]]
}

$g.Dispose()

# ---- outputs ----
$outRoot = "C:\Users\TAJ PC\Music\VICTOR FLAGSA\android\app\src\main\res"
$densities = @{ mdpi = 48; hdpi = 72; xhdpi = 96; xxhdpi = 144; xxxhdpi = 192 }

function Save-Scaled {
    param($src, $size, $file, $circle)
    $dst = New-Object System.Drawing.Bitmap($size, $size)
    $og = [System.Drawing.Graphics]::FromImage($dst)
    $og.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $og.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    if ($circle) {
        $p = New-Object System.Drawing.Drawing2D.GraphicsPath
        $p.AddEllipse(0, 0, $size, $size)
        $og.SetClip($p)
    }
    $og.DrawImage($src, 0, 0, $size, $size)
    $og.Dispose()
    $dst.Save($file, [System.Drawing.Imaging.ImageFormat]::Png)
    $dst.Dispose()
}

foreach ($d in $densities.Keys) {
    $px = $densities[$d]
    $dir = Join-Path $outRoot "mipmap-$d"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    Save-Scaled $bmp $px (Join-Path $dir "ic_launcher.png") $false
    Save-Scaled $bmp $px (Join-Path $dir "ic_launcher_round.png") $true

    # adaptive foreground: keep art inside the 66/108 safe zone
    $fgPx = [int][Math]::Round($px * (108.0 / 48.0))
    $fg = New-Object System.Drawing.Bitmap($fgPx, $fgPx)
    $og = [System.Drawing.Graphics]::FromImage($fg)
    $og.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $inner = [int][Math]::Round($fgPx * 0.62)
    $off = [int](($fgPx - $inner) / 2)
    $og.DrawImage($bmp, $off, $off, $inner, $inner)
    $og.Dispose()
    $fg.Save((Join-Path $dir "ic_launcher_foreground.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $fg.Dispose()
    Write-Host "$d -> $px px legacy, $fgPx px foreground"
}

# 512x512 icon for the Play Console store listing
$listing = Join-Path $outRoot "..\..\..\..\play"
New-Item -ItemType Directory -Force -Path $listing | Out-Null
Save-Scaled $bmp 512 (Join-Path $listing "play_store_icon_512.png") $false
$bmp.Dispose()
Write-Host "done"
