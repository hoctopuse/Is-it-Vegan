#!/usr/bin/env python3
"""Add reviewed CELEX 02013R1308 product terms from four local PDFs only."""
from __future__ import annotations
import argparse, json, re, unicodedata
from pathlib import Path
from pypdf import PdfReader

ROOT=Path(__file__).resolve().parents[1]; BASE=ROOT/'reference-input/eu-food-labelling/02-sector-product-standards'
PDFS={x:BASE/f'CELEX_02013R1308-20260818_{x}_TXT.pdf' for x in ('FR','NL','EN','DE')}
I=ROOT/'knowledge/ingredients.json'; L=ROOT/'knowledge/ingredient_aliases_multilingual.json'; S=ROOT/'knowledge/sources.json'
SOURCE={'id':'eu-agricultural-products-regulation-1308-2013-20260818','title':'Regulation (EU) No 1308/2013 — common organisation of the markets in agricultural products','celex':'02013R1308','url':'https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02013R1308-20260818','language':'FR, NL, EN, DE','institution':'European Parliament and Council of the European Union','consolidationDate':'2026-08-18','localPdfPaths':[str(x.relative_to(ROOT)).replace('\\','/') for x in PDFS.values()],'role':'Primary local source for Annex I agricultural sector names and Annex VII product definitions.','limitations':'Article 1 excludes fishery and aquaculture products. Regulatory sectors identify agricultural goods but do not establish the composition of a general commercial preparation.'}
A={
'meat':{'FR':['viande bovine','viande de porc','viandes ovine et caprine','viande de volaille'],'NL':['rundvlees','varkensvlees','schapen- en geitenvlees','pluimveevlees'],'EN':['beef and veal','pigmeat','sheepmeat and goatmeat','poultrymeat'],'DE':['Rindfleisch','Schweinefleisch','Schaf- und Ziegenfleisch','Geflügelfleisch']},
'edible_offal':{'FR':["abats comestibles des animaux de l'espèce bovine"],'NL':['eetbare slachtafvallen van runderen'],'EN':['edible offal of bovine animals'],'DE':['Genießbare Schlachtnebenerzeugnisse von Rindern']},
'animal_fat':{'FR':['graisses de porc (y compris le saindoux)'],'NL':['varkensvet (reuzel daaronder begrepen)'],'EN':['pig fat (including lard)'],'DE':['Schweinefett (einschließlich Schweineschmalz)']},
'poultry_meat_preparation':{'FR':['préparation à base de viande de volaille'],'NL':['bereiding op basis van pluimveevlees'],'EN':['poultrymeat preparation'],'DE':['Geflügelfleischzubereitungen']},
'processed_fruit_vegetable_product':{'FR':['produits transformés à base de fruits et légumes'],'NL':['verwerkte groenten en fruit'],'EN':['processed fruit and vegetable products'],'DE':['Verarbeitungserzeugnisse aus Obst und Gemüse']},
'spreadable_fat':{'FR':['matières grasses tartinables'],'NL':['smeerbare vetten'],'EN':['spreadable fats'],'DE':['Streichfette']},
'egg':{'FR':["œufs de volailles de basse-cour, en coquille"],'NL':['eieren van pluimvee in de schaal'],'EN':['poultry eggs, in shell'],'DE':['Eier von Hausgeflügel in der Schale']},
'olive_oil':{'FR':["huile d'olive"],'NL':['olijfolie'],'EN':['olive oil'],'DE':['Olivenöl']},
}
NEW={'edible_offal':('Abats comestibles','NON_VEGAN','Parties comestibles d’animaux : non vegan.'),'animal_fat':('Graisse animale','NON_VEGAN','Graisse explicitement issue d’animaux : non vegan.'),'poultry_meat_preparation':('Préparation de viande de volaille','NON_VEGAN','Préparation explicitement à base de viande de volaille : non vegan.'),'processed_fruit_vegetable_product':('Produit transformé à base de fruits et légumes','UNCERTAIN','Catégorie réglementaire de produit transformé : lire la liste complète des ingrédients.'),'spreadable_fat':('Matière grasse tartinable','UNCERTAIN','Peut être végétale, animale ou mélangée selon la composition.')}
def n(x):
 x=unicodedata.normalize('NFKD',x.casefold());return re.sub(r'[^a-z0-9]+',' ',''.join(c for c in x if not unicodedata.combining(c))).strip()
def compact(x):return re.sub(r'\s+','',n(x))
def text(p):return '\n'.join(x.extract_text() or '' for x in PdfReader(p).pages)
def main():
 p=argparse.ArgumentParser();g=p.add_mutually_exclusive_group(required=True);g.add_argument('--dry-run',action='store_true');g.add_argument('--write',action='store_true');g.add_argument('--check',action='store_true');a=p.parse_args()
 ts={lang:compact(text(path)) for lang,path in PDFS.items()}; missing=[f'{k}/{lang}/{x}' for k,v in A.items() for lang,xs in v.items() for x in xs if compact(x) not in ts[lang]]
 if missing:raise ValueError('unverified local-PDF aliases: '+str(missing))
 db=json.loads(I.read_text(encoding='utf8'));lex=json.loads(L.read_text(encoding='utf8'));sources=json.loads(S.read_text(encoding='utf8'))
 if len(db)<459 or len(lex.get('mappings',[]))<1864:raise ValueError('historical knowledge is missing; refusing destructive import')
 ids={x['id'] for x in db}; owners={n(z):x['id'] for x in db for z in x.get('aliases',[])}; lexowners={(x['language'],n(z)):x['canonicalId'] for x in lex['aliases'] for z in x.get('aliases',[])+x.get('ocrVariants',[])}; keys={(x['conceptId'],x['language'],x['normalizedForm']) for x in lex.get('mappings',[])}; changed=False
 for k,langs in A.items():
  if k not in ids:
   name,status,reason=NEW[k]; entry={'id':k,'name':name,'aliases':[],'status':status,'reason':reason,'sources':[SOURCE['id']]}
   if k in {'processed_fruit_vegetable_product','spreadable_fat'}:entry['possibleOriginNote']={'origins':['PLANT','ANIMAL'],'variability':'MANUFACTURER','sourceIds':[SOURCE['id']],'confidence':'REGULATORY_CATEGORY'}
   db.append(entry);ids.add(k);changed=True
  entry=next(x for x in db if x['id']==k)
  if k=='cheese' and entry['status']=='UNCERTAIN': entry['status']='VEGETARIAN';entry['reason']='Produit laitier d’origine animale : végétarien mais non vegan.';changed=True
  if SOURCE['id'] not in entry['sources']:entry['sources'].append(SOURCE['id']);changed=True
  for lang,xs in langs.items():
   le=next((x for x in lex['aliases'] if x['canonicalId']==k and x['language']==lang),None)
   if not le:le={'canonicalId':k,'language':lang,'aliases':[],'ocrVariants':[]};lex['aliases'].append(le);changed=True
   for x in xs:
    if owners.get(n(x),k)!=k or lexowners.get((lang,n(x)),k)!=k:raise ValueError(f'alias collision: {lang}/{x}')
    if x not in entry['aliases']:entry['aliases'].append(x);changed=True
    if x not in le['aliases']:le['aliases'].append(x);changed=True
    r={'surfaceForm':x,'language':lang,'conceptId':k,'mappingGroup':'agricultural-products-regulation','relation':'REGULATORY_ALIAS','normalizedForm':n(x),'source':SOURCE['id'],'confidence':'REVIEWED'}; key=(k,lang,r['normalizedForm'])
    if key not in keys:lex.setdefault('mappings',[]).append(r);keys.add(key);changed=True
 for k in ('milk','cream','butter','whey','casein','lactose','cheese'):
  e=next(x for x in db if x['id']==k)
  if SOURCE['id'] not in e['sources']:e['sources'].append(SOURCE['id']);changed=True
 if not any(x['id']==SOURCE['id'] for x in sources):sources.append(SOURCE);changed=True
 print(f'CELEX 02013R1308: {len(NEW)} concepts, {sum(len(x) for v in A.values() for x in v.values())} aliases, changes={int(changed)}')
 if a.check and changed:raise SystemExit('agricultural regulation import is stale; run --write')
 if a.write and changed:
  I.write_text(json.dumps(db,ensure_ascii=False,indent=2)+'\n',encoding='utf8');L.write_text(json.dumps(lex,ensure_ascii=False,indent=2)+'\n',encoding='utf8');S.write_text(json.dumps(sources,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
if __name__=='__main__':main()
