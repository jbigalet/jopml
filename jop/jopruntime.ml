module IntMap = Map.Make(struct type t = int let compare = compare end)
module IntSet = Set.Make(struct type t = int let compare = compare end)
module IPSet = Set.Make(struct type t = int*int let compare = compare end)

module Uf = struct
(*  type t = (int IntMap.t)*(int IntMap.t)

  let empty = (IntMap.empty, IntMap.empty)

  let rank (m,r) a =
    IntMap.find a r

  let create (m,r) x =
    (IntMap.add x x m, IntMap.add x 0 r)

  let rec find (m,r) a =
    try
      let p = IntMap.find a m in
      if p = a then ((m,r), a)
      else let (m,r), p' = find (m,r) p in
	   ((IntMap.add a p' m, r), p')
    with Not_found -> (create (m,r) a, a)

  let union s a b =
    let s, a' = find s a in
    let s, b' = find s b in
    let incr_rank x =
      IntMap.add x (IntMap.find x (snd s)) (snd s)
    in
    let ra = rank s a' and rb = rank s b' in
    let (m,r) = s in
    if ra = rb then
      (IntMap.add a' b' m, incr_rank a')
    else if ra > rb then
      (IntMap.add b' a' m, r)
    else
      (IntMap.add a' b' m, r)

  let has (m,r) x = IntMap.mem x m

  let remove (m,r) x = (IntMap.remove x m, IntMap.remove x r)

  let merge s s' =
    let res = ref s in
    IntMap.iter (fun k _ -> res := union !res k (snd (find s' k))) (snd s');
    !res
*)
  type 'a t = { p: ('a,'a) Hashtbl.t; rks: ('a,int) Hashtbl.t }
  let create_struct () =
    { p = Hashtbl.create 54651; rks = Hashtbl.create 54641 }

  let create s x =
    Hashtbl.add s.p x x;
    Hashtbl.add s.rks x 0

  let rec find s x =
    let p = Hashtbl.find s.p x in
    if p = x then x
    else begin
      let r_p = find s p in
      Hashtbl.add s.p x r_p;
      r_p
    end
  let rank s x =
    Hashtbl.find s.rks x
  let incr_rank s x =
    Hashtbl.add s.rks x ((Hashtbl.find s.rks x) + 1)
  let remove s x =
    Hashtbl.remove s.p x;
    Hashtbl.remove s.rks x
  let union s x y =
    let r_x = find s x and r_y = find s y in
    let rk_x = rank s x and rk_y = rank s y in
    if rk_x = rk_y then (
      Hashtbl.add s.p r_y r_x;
      incr_rank s r_x
    )
    else if rk_x > rk_y then
      Hashtbl.add s.p r_y r_x
    else
      Hashtbl.add s.p r_x r_y
    
end

module Circuit = struct
  type ram_desc = { ram_size: int; addr_size: int; data_size: int; ram_name: string }
  type gate_type = AND | NOT | XOR | ONE | ZERO | REG | OR | INPUT of int | OUTPUT of int | RAM of ram_desc
  type gate = { t: gate_type; inputs: int list; outputs: int list; name: string option }
  module GateSet = Set.Make(struct type t = gate let compare = compare end)

  type circuit = { gates: GateSet.t }
  type pr = circuit*(int list)

  let mappings = Uf.create_struct ()

  let port_num = (ref 0)
    

  let remove x = ((*Printf.printf "deleted !\n"; List.iter (Uf.remove mappings) x*))
  let create_port c = begin
    incr port_num;
    Uf.create mappings !port_num;
    !port_num
  end
    
  let empty = { gates = GateSet.empty }

  let create_ports n =
    let rec aux acc = function
      | 0 -> acc
      | n -> aux ((create_port ())::acc) (n-1)
    in (empty, aux [] n)
    
  let num_input = function
    | AND | XOR | OR -> 2
    | NOT | REG -> 1
    | ONE | ZERO -> 0
    | INPUT _ -> 0
    | OUTPUT n -> n
    | RAM ramdesc -> (*write_flag*) 1 + (*addr*)ramdesc.addr_size + (*write_data*)ramdesc.data_size
      
  let num_output = function
    | AND | XOR | NOT | ONE | ZERO | REG | OR -> 1
    | OUTPUT _ -> 0
    | INPUT n -> n
    | RAM ramdesc -> (*read_data*)ramdesc.data_size



 
      
  let used_ports c =
    GateSet.fold (fun g ps ->
      List.fold_left (fun ps i ->
	IntSet.add i ps
      ) ps (g.inputs@g.outputs)
    ) c.gates IntSet.empty

  let rec merge (a,pa) (b, pb) =
    ({ gates = GateSet.union a.gates b.gates }, pa@pb)

  let map c p q = begin
    Uf.union mappings p q;
    c
  end

 let add_gate t (c,inputs) name = 
    let (_,outputs) = create_ports (num_output t) in
    merge (c,[]) ({
      gates = GateSet.singleton {
	t = t; inputs = inputs; outputs = outputs; name = name
      }}, outputs)
      
  let eliminate_mappings c =
    let gs_map f = GateSet.fold (fun x s -> GateSet.add (f x) (GateSet.remove x s)) c.gates c.gates in
    let elim = List.map (fun i -> Uf.find mappings i) in
    let gates =
      gs_map (fun g ->
	{ t = g.t; name = g.name;
	  inputs = elim g.inputs;
	  outputs = elim g.outputs
	})
    in
    { gates = gates }

  let rec map_all (cp,p) (cq, q) =
    let rec aux c = function
	| p::ps, q::qs -> aux (map c p q) (ps, qs)
	| [],[] -> c
	| _ -> failwith (Printf.sprintf "Erreur taille d'arguments : %d/%d" (List.length p) (List.length q))
    in merge (cp,[]) ((aux cq (p,q)), [])

  let rec flatten = function
    | [] -> (empty, [])
    | c::q -> let c' = flatten q in
		  merge c c'
end

module CycleCheck = struct
  open Circuit
  type node = Gate of Circuit.gate | Port of int
  module NodeSet = Set.Make(struct
    type t = node
    let compare a b = match (a,b) with
      | Port i, Port j -> compare i j
      | Gate g1, Gate g2 -> compare g1 g2
      | Port _, Gate _ -> -1
      | Gate _, Port _ -> 1
  end)
  let order c =
    let c = Circuit.eliminate_mappings c in 
    let gs = GateSet.elements c.gates in
    let add_in p i m =
      let nl = i::(
	try
	  IntMap.find p m
	with Not_found -> []
      ) in
      IntMap.add p nl m
    in
    let ins_connected = List.fold_left (fun m g ->
      List.fold_left (fun m' i ->
	add_in i (Gate g) m'
      ) m g.inputs
    ) IntMap.empty (List.filter (fun g -> g.t <> REG) gs)
    in
    let v = function
      | Gate g -> List.map (fun i -> Port i) g.outputs
      | Port i -> try
		    IntMap.find i ins_connected
	with Not_found -> []
    in
    let port_nodes = IntSet.fold (fun i ns -> NodeSet.add (Port i) ns) (used_ports c) NodeSet.empty in
    let nodes = List.fold_left (fun ns g -> NodeSet.add (Gate g) ns) port_nodes gs in
    let rec aux graph =
      let v' u = List.filter (fun n -> NodeSet.mem n graph) (v u) in
      if NodeSet.is_empty graph then [] else
	let nosucc, rest = NodeSet.partition (fun n -> v' n = []) graph in
	if NodeSet.is_empty nosucc then failwith "aarghl"
	else (List.fold_left (fun l x -> match x with
	  | Gate g -> g::l
	  | Port _ -> l
	) (aux rest) (NodeSet.elements nosucc))
    in List.rev (aux nodes)
end

module NetList = struct
  type gate = { t: Circuit.gate_type; id: int; inputs: int array; outputs: int array }
  type netlist = gate array
      
  exception Combinatorial_loop

  let generate =
    CycleCheck.order

end

open Printf
open Circuit



let generate (c, _) fn =
  printf "Starting generation\n";
  let nl = NetList.generate c in
  printf "Generated\n";
  let name g = match g.name with
    | Some s -> s
    | _ -> "no_name"
  in
  let f = open_out fn in
  let print_ls = List.iter (fun s -> fprintf f "%d " s) in
  List.iter (fun g ->
      fprintf f "%s " (match g.t with
	|  AND -> "and" | NOT -> "not" | XOR -> "xor" | ONE -> "one" | ZERO -> "zero" | REG -> "reg" | OR -> "or"
	| INPUT _ -> "input @" ^ (name g) | OUTPUT _ -> "output @" ^ (name g)
	| RAM ramdesc -> sprintf "ram @%s %d %d %d" (name g) ramdesc.data_size ramdesc.addr_size ramdesc.ram_size
      );
      print_ls g.inputs;
      print_ls g.outputs;
      fprintf f "\n";
    ) nl
let temp_gen c fn =
  generate (c, []) fn

let extract_range (c, ls) i j =
  let rec aux n acc = function
    | [] -> acc
    | x::xs -> aux (n+1) (if (n >= i && n <= j || i = -1) then x::acc else acc) xs
  in (c, List.rev (aux 0 [] ls))

let base_gate = add_gate;;
let xor_ a b = base_gate XOR (flatten [a; b]) None;;
let and_ a b = base_gate AND (flatten [a; b]) None;;
let or_ a b = base_gate OR (flatten [a;b]) None;;
let input s n = base_gate (INPUT n) (flatten []) (Some s);;
let output s l = base_gate (OUTPUT (List.length (snd l))) l (Some s);;
let reg a = base_gate REG a None;;
let not_ a = base_gate NOT a None;;
let one () = base_gate ONE (flatten []) None;;
let zero () = base_gate ZERO (flatten []) None;;
let ram ram_desc addr write_flag write_data = base_gate (RAM ram_desc) (flatten [write_flag; addr; write_data]) (Some ram_desc.ram_name);;
let tl (c, l)= (c, List.tl l)
let hd (c,l) = (c, [List.hd l])
let ls (c, l) = List.map (fun i -> (c,[i])) l
let len (c,l) = List.length l
let rec binary = function
  | 0 -> []
  | n when n mod 2 = 0 -> 0::(binary (n/2))
  | n -> 1::(binary ((n - 1)/2))
let bitsize n = List.length (binary n)
