const fs = require("fs");
const codes = "af,al,dz,ad,ao,ag,ar,am,au,at,az,bs,bh,bd,bb,by,be,bz,bj,bt,bo,ba,bw,br,bn,bg,bf,bi,cv,kh,cm,ca,cf,td,cl,cn,co,km,cg,cd,cr,ci,hr,cu,cy,cz,dk,dj,dm,do,ec,eg,sv,gq,er,ee,sz,et,fj,fi,fr,ga,gm,ge,de,gh,gr,gd,gt,gn,gw,gy,ht,hn,hu,is,in,id,ir,iq,ie,il,it,jm,jp,jo,kz,ke,ki,kw,kg,la,lv,lb,ls,lr,ly,li,lt,lu,mg,mw,my,mv,ml,mt,mh,mr,mu,mx,fm,md,mc,mn,me,ma,mz,mm,na,nr,np,nl,nz,ni,ne,ng,kp,mk,no,om,pk,pw,pa,pg,py,pe,ph,pl,pt,qa,kr,ro,ru,rw,kn,lc,vc,ws,sm,st,sa,sn,rs,sc,sl,sg,sk,si,sb,so,za,ss,es,lk,sd,sr,se,ch,sy,tj,tz,th,tl,tg,to,tt,tn,tr,tm,tv,ug,ua,ae,gb,us,uy,uz,vu,ve,vn,ye,zm,zw,ps,va,tw".split(",");

let xml = `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:image="http://www.google.com/schemas/sitemap-image/1.1">\n`;

const pages = [
  { loc: "https://countryflagbattles.vercel.app/", priority: "1.0", changefreq: "daily" },
  { loc: "https://countryflagbattles.vercel.app/index.html", priority: "0.9", changefreq: "daily" },
  { loc: "https://countryflagbattles.vercel.app/about", priority: "0.8", changefreq: "monthly" },
  { loc: "https://countryflagbattles.vercel.app/about.html", priority: "0.7", changefreq: "monthly" },
  { loc: "https://countryflagbattles.vercel.app/privacy", priority: "0.9", changefreq: "monthly" },
  { loc: "https://countryflagbattles.vercel.app/privacy.html", priority: "0.8", changefreq: "monthly" },
  { loc: "https://countryflagbattles.vercel.app/privacy-policy.html", priority: "0.8", changefreq: "monthly" },
  { loc: "https://countryflagbattles.vercel.app/terms", priority: "0.7", changefreq: "monthly" },
  { loc: "https://countryflagbattles.vercel.app/terms.html", priority: "0.6", changefreq: "monthly" },
  { loc: "https://countryflagbattles.vercel.app/contact", priority: "0.7", changefreq: "monthly" },
  { loc: "https://countryflagbattles.vercel.app/contact.html", priority: "0.6", changefreq: "monthly" }
];

for (const p of pages) {
  xml += `  <url>\n    <loc>${p.loc}</loc>\n    <lastmod>2026-11-06</lastmod>\n    <changefreq>${p.changefreq}</changefreq>\n    <priority>${p.priority}</priority>\n`;
  if (p.loc === "https://countryflagbattles.vercel.app/" || p.loc === "https://countryflagbattles.vercel.app/index.html") {
    for (const c of codes) {
      xml += `    <image:image>\n      <image:loc>https://countryflagbattles.vercel.app/flags/${c}.png</image:loc>\n      <image:title>Flag of ${c.toUpperCase()}</image:title>\n    </image:image>\n`;
    }
  }
  xml += `  </url>\n`;
}

xml += `</urlset>\n`;
fs.writeFileSync("docs/sitemap.xml", xml);
fs.writeFileSync("sitemap.xml", xml);
console.log("Successfully generated sitemap.xml for docs/ and root!");
