open Camlp4.PreCast;;
open Syntax;;

type port = { name: string; st: Ast.expr option; ed: Ast.expr option }

let node_list _loc el =
  List.fold_right (fun x l -> <:expr< $x$ :: $l$ >>) el <:expr< [] >>

let zero _loc = <:expr<0>>

let convert_to_range _loc port =
  match (port.st, port.ed) with
    | Some e, Some e' -> e,e'
    | Some e, None -> e,e
    | None, None -> <:expr<-1>>, <:expr<-1>>
  

let convert_to_size _loc port =
  match (port.st, port.ed) with
    | Some e, Some e' -> <:expr<$e'$ - $e$ + 1>>
    | Some e, None -> <:expr<$e$>>
    | None, None -> <:expr<1>>
      
let rec decl_var_list _loc e = function
  | [] -> e
  | v::vs ->
    let size = convert_to_size _loc v in
    <:expr<let $lid:v.name$ = create_ports $size$ in $decl_var_list _loc e vs$>>
   
let rec finalize_var_list _loc e = function
  | [] -> e
  | v::vs ->
    <:expr<(Gc.finalise (fun _ -> Circuit.remove (snd $lid:v.name$)) _ci; $finalize_var_list _loc e vs$)>>

let rec  generate_mappings _loc c x =
  let nl = node_list _loc (List.map (fun p ->
    let st,ed = convert_to_range _loc p in
    <:expr<extract_range $lid:p.name$ $st$ $ed$>>) x)
  in
  <:expr<Circuit.map_all
	   (flatten $nl$) $c$>>

EXTEND Gram
GLOBAL: expr;

port:
  [
    [ x = LIDENT ; r = OPT [ "["; idx = expr; endopt = OPT[":"; e = expr -> e]; "]" -> (idx, endopt)]
      -> match r with
	| None -> { name = x; st = None; ed = None }
	| Some (s,e) -> { name = x; st = Some s; ed = e }
    ]
  ];
portlist:
  [
    [ x = LIST0[ y = port -> y ] SEP "," -> x]
  ];
circuit:
  [ LEFTA 
      [ x = circuit; "|"; y = circuit -> <:expr<Circuit.merge $x$ $y$>> ]
  | [ x = portlist; "="; y = expr -> generate_mappings _loc y x]
  | [ x = expr -> <:expr<$x$>> ]
  ];
expr: BEFORE "apply"
  [
    [ "<"; x =  portlist;
      y = [
	";"; z = portlist ;">"-> z
      | ">" -> []];
      "|"; s = circuit ->
       let l =  (List.map (fun p -> <:expr<$lid:p.name$>>) x) in
       let l = (<:expr<_ci>>)::l in
       let end_expr = <:expr<flatten $node_list _loc l$>> in
       let finalized = end_expr(*finalize_var_list _loc end_expr (x@y)*) in
       decl_var_list _loc <:expr<let _ci = $s$ in $finalized$>> (x@y)
    ]
  |    [ "|"; x = circuit -> <:expr<($x$, [])>> ]
  ];
END;;
