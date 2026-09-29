#!/usr/bin/env python3
"""Import CELEX 02001L0112 generic fruit-product names from local PDFs."""
import argparse,json,re,unicodedata
from pathlib import Path
from pypdf import PdfReader
R=Path(__file__).resolve().parents[1]; B=R/'reference-input/eu-food-labelling/02-sector-product-standards'
P={l:B/f'CELEX_02001L0112-20260614_{l}_TXT.pdf' for l in ('FR','NL','EN','DE')}
I=R/'knowledge/ingredients.json'; L=R/'knowledge/ingredient_aliases_multilingual.json'; S=R/'knowledge/sources.json'
A={
'fruit_juice':{'FR':['jus de fruits','jus de fruits à base de concentré','jus de fruits concentré','jus de fruits obtenu par extraction hydrique','jus de fruits déshydraté/en poudre'],'NL':['vruchtensap','vruchtensap uit concentraat','geconcentreerd vruchtensap'],'EN':['fruit juice','fruit juice from concentrate','concentrated fruit juice','water extracted fruit juice','dehydrated/powdered fruit juice'],'DE':['Fruchtsaft','Fruchtsaft aus Fruchtsaftkonzentrat','Fruchtsaftkonzentrat']},
'fruit_puree':{'FR':['purée de fruits','purée de fruits concentrée'],'NL':['vruchtenpuree'],'EN':['fruit purée','concentrated fruit purée'],'DE':['Fruchtmark']},
'fruit_nectar':{'FR':['nectar de fruits'],'NL':['vruchtennectar'],'EN':['fruit nectar'],'DE':['Fruchtnektar']}}
SRC={'id':'eu-fruit-juice-directive-2001-112-20260614','title':'Council Directive 2001/112/EC — fruit juices and certain similar products','celex':'02001L0112','url':'https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02001L0112-20260614','language':'FR, NL, EN, DE','institution':'Council of the European Union','consolidationDate':'2026-06-14','localPdfPaths':[str(x.relative_to(R)).replace('\\','/') for x in P.values()],'role':'Primary local source for Annex I fruit juice product names.','limitations':'Product definitions do not make fruit nectar vegan: it may contain honey, sugars or sweeteners.'}
def n(s): return ''.join(c for c in unicodedata.normalize('NFD',unicodedata.normalize('NFKC',s).casefold()) if not unicodedata.combining(c))
def c(s): return re.sub(r'\s+','',n(s).replace('\u00ad',''))
def main():
 q=argparse.ArgumentParser();q.add_argument('--dry-run',action='store_true');q.add_argument('--write',action='store_true');q.add_argument('--check',action='store_true');a=q.parse_args()
 if sum((a.dry_run,a.write,a.check))!=1:q.error('choose exactly one mode')
 texts={l:c('\n'.join(x.extract_text() or '' for x in PdfReader(p).pages)) for l,p in P.items()}
 miss=[f'{k}/{l}/{x}' for k,v in A.items() for l,xs in v.items() for x in xs if c(x) not in texts[l]]
 if miss: raise ValueError('unverified PDF terms: '+str(miss))
 db=json.loads(I.read_text(encoding='utf8')); lex=json.loads(L.read_text(encoding='utf8')); src=json.loads(S.read_text(encoding='utf8')); changed=False
 status={'fruit_juice':'VEGAN','fruit_puree':'VEGAN','fruit_nectar':'UNCERTAIN'}
 reason={'fruit_juice':'Produit réglementaire obtenu à partir de parties comestibles de fruits : vegan.','fruit_puree':'Produit végétal obtenu à partir de parties comestibles de fruits : vegan.','fruit_nectar':'Peut légalement contenir du miel, des sucres ou des édulcorants : la dénomination seule ne confirme pas un produit vegan.'}
 for k,langs in A.items():
  e=next((x for x in db if x['id']==k),None)
  if not e: e={'id':k,'name':{'fruit_juice':'Jus de fruits','fruit_puree':'Purée de fruits','fruit_nectar':'Nectar de fruits'}[k],'aliases':[],'status':status[k],'reason':reason[k],'sources':[SRC['id']]};db.append(e);changed=True
  for l,xs in langs.items():
   le=next((x for x in lex['aliases'] if x['canonicalId']==k and x['language']==l),None)
   if not le: le={'canonicalId':k,'language':l,'aliases':[],'ocrVariants':[]};lex['aliases'].append(le);changed=True
   for x in xs:
    if x not in e['aliases']:e['aliases'].append(x);changed=True
    if x not in le['aliases']:le['aliases'].append(x);changed=True
 if not any(x['id']==SRC['id'] for x in src):src.append(SRC);changed=True
 print('CELEX 02001L0112:',sum(len(x) for v in A.values() for x in v.values()),'aliases; changes='+str(int(changed)))
 if a.check and changed: raise SystemExit('fruit juice import is stale; run --write')
 if a.write and changed:
  I.write_text(json.dumps(db,ensure_ascii=False,indent=2)+'\n',encoding='utf8');L.write_text(json.dumps(lex,ensure_ascii=False,indent=2)+'\n',encoding='utf8');S.write_text(json.dumps(src,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
if __name__=='__main__':main()
