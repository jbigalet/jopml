open Jopruntime
open Jopruntime.Circuit

let rec const_binary = function
  | [] -> failwith "pas de const vide"
  | [x] -> if x = 0 then zero () else one ()
  | x::xs -> (<;> | (const_binary [x]) | (const_binary xs))

let pad_right x s =
    let rec aux x = function
      | 0 -> x
      | n when n < 0 -> failwith "Trop long pour le padding"
      | n -> aux (x@[0]) (n-1)
    in aux x (s - (List.length x))

let rec const_int padding n =
  let b = pad_right (binary n) padding in
  const_binary b
let word_size = int_of_string Sys.argv.(1)
let const_word = const_int word_size
let half_add a b =
  < s,c >
    | s = xor_ a b
    | c = and_ a b


let full_add a b ci =
  < s,co ; u,v,w >
    | u,v = half_add a b
    | s,w = half_add u ci
    | co = or_ w v


let bitwise op a b =
  let rec aux = function
    | [],[] -> failwith "bitwise vide interdit"
    | [x],[y] -> op x y
    | x::xs,y::ys -> (<;> | (op x y) | (aux (xs,ys)))
    | _ -> failwith "pas la même taille"
  in aux (ls a,ls b)

let rec dup p = function
  | 0 -> (empty, [])
  | n -> (<;> | (p) | (dup p (n-1)))

let no_overflow c =
  <;> | (c) | (zero ())
let bw_and = bitwise and_
let bw_or = bitwise or_
let bw_xor = bitwise xor_
let bw_not = bitwise (fun a b -> not_ a)

let bw_and_ovf a b = no_overflow (bw_and a b)
let bw_or_ovf a b = no_overflow (bw_or a b)
let bw_xor_ovf a b = no_overflow (bw_xor a b)
let bw_not_ovf a b = no_overflow (bw_not a b)

let adder_with_carry carry a b =
  let rec aux n ci_in = function
    | [],[] -> ci_in
    | p::ps,q::qs ->
      (
	< out[0:n-1], ci_out; ci_t>
	  | out[0], ci_t = full_add p q ci_in
	  | out[1:(n-1)], ci_out = aux (n-1) ci_t (ps,qs)
      )
    | _ -> assert false
  in let la = len a and lb = len b in
     if la != lb then failwith "Pas la même taille"
     else aux la carry (ls a, ls b)

let adder a b =
  adder_with_carry (zero ()) a b

let mul a b =
  let prod1 ai b = bw_and (dup ai word_size) b in
  let shift_up c n =
   ( <s[word_size];dummy[n]>
       | s[0:n-1] = dup (zero ()) n
       | s[n:word_size], dummy = c
   ) in
  let products = List.fold_left (fun ps ai -> (prod1 ai b)::ps) [] (ls a) in
  List.fold_left (fun c pi ->
    <a[word_size],r;ci,c'[word_size],ci'> | c',ci = c | a,ci' = adder pi (shift_up c' 1) | r = or_ ci' ci
  ) (<;> | (List.hd products) | (zero ())) (List.tl products)

let select2 in1 in2 sel =
  let sel1 i j =
    or_ (and_ i (not_ sel)) (and_ j sel)
  in
  let rec aux = function
    | [],[] -> failwith "Pas de selecteur vide"
    | [x],[y] -> sel1 x y
    | x::xs,y::ys -> (<;> | (sel1 x y) | (aux (xs,ys)))
    | _ -> failwith "pas la même taille"
  in aux (ls in1, ls in2)


(*
  ins: (portrange*int) list
  a chaque portrange d'entrée on associe le numero auquel il doit correspondre lors de la selection
  sel: portange de selection
*)
let select (ins:(pr*int) list) sel =
  let ins = List.map (fun (p,i) -> (p, binary i)) ins in
  let sel_size = len sel in
  let in_sel_size = List.fold_left max 0 (List.map (fun (_,b) -> List.length b) ins) in
  if sel_size < in_sel_size then failwith "selecteur trop petit";
  let ins = List.map (fun (p,b) -> (p, pad_right b sel_size)) ins in
  let in_sizes = List.map (fun (p,b) -> len p) ins in
  let in_size = List.hd in_sizes in
  if List.exists (fun s -> s != in_size) in_sizes then failwith "Les inputs n'ont pas tous la même taille";
  let filter x l = List.map
    (fun (p,b) -> (p, List.tl b))
    (List.filter (fun (p,b) -> b != [] && ((List.hd b) = x)) l)
  in
  let const_0 = const_int in_size 0 in
  let rec aux ins selectors =
    let l0 = filter 0 ins and l1 = filter 1 ins in
    let n0 = List.length l0 and n1 = List.length l1 in
    if n0 = 0 && n1 = 0 then const_0
    else match selectors with
      | [sel] ->
	let total = List.length ins in
	if total > 2 then assert false
	else if List.exists (fun (p,b) -> List.length b > 1) ins then
	  failwith "une des repr. binaire est trop longue"
	else (
	  if n0 > 1 or n1 > 1 then failwith "Deux inputs ont le même numero"
	  else (
	    let p0 = (if l0 = [] then const_0 else fst (List.hd l0))
	    and p1 = (if l1 = [] then const_0 else fst (List.hd l1)) in
	    select2 p0 p1 sel
	  )
	)
      | sel::sels ->
	let s0 = aux l0 sels and s1 = aux l1 sels in
	select2 s0 s1 sel
      | _ -> failwith "pas fait"
  in
  aux ins (ls sel)


let register write overwrite =
  assert ((len write) = word_size);
  let reg1 w =
    <u>
    | u = reg (select2 u w overwrite)
  in
  List.fold_left
    (fun c w ->
      <;> | (c) | (reg1 w)
    ) (empty, []) (ls write)

let incrementing_register write overwrite =
  let reg1 a w =
    < c,s >
    | s = reg (select2 (xor_ a s) w overwrite)
    | c = and_ a s
  in
  let rec aux a = function
    | [] -> failwith "ploop"
    | [w] -> reg1 a w
    | w::ws -> let n = List.length ws in
      ( <c_out, s[n+1]; c_temp>
	  | c_temp, s[0] = reg1 a w
	  | c_out, s[1:n] = aux c_temp ws
      )
  in tl (aux (one ()) (ls write))

let is_positive u _ =
  dup (extract_range u (word_size - 1) (word_size - 1)) word_size

let negation u _ =
  adder (bw_not u u) (const_word 1)

let sub u v =
  <;n[word_size],ovf>
    | n,ovf = negation v v
    | (adder u n)

type alu_modules_codes = 
    {
      add: int;
      neg: int;
      sub: int;
      mul:int;
      bw_or: int;
      bw_and: int;
      bw_not: int;
      bw_xor: int;
      is_pos: int;
    }

let alu input1 input2 sel codes =
  assert (len input1 = word_size
	 && len input2 = word_size);
  let modules = [
    adder, codes.add;
    negation, codes.neg;
    sub, codes.sub;
    bw_or_ovf, codes.bw_or;
    bw_and_ovf, codes.bw_and;
    bw_xor_ovf, codes.bw_xor;
    bw_not_ovf, codes.bw_not;
    mul, codes.mul;
    (fun a _ -> no_overflow (is_positive a a)), codes.is_pos
  ] in
  let modules = List.map (fun (f,code) -> (f input1 input2, code)) modules in
  select modules sel

(* Crée une unitée de <count> registres
   où tous sont connectés en entées sur chaque circuits de writes, en entrée sur chacun des reads etc.
*)
let make_register_unit registers reads write overwrite =
  let make_reg register read overwrite =
    <;> | read = register write overwrite
  in
  let rec aux = function
    | reg::regs, r::rs, o::os -> merge (make_reg reg r o) (aux (regs,rs,os))
    | [], [],[] -> (empty,[])
    | _ -> failwith "pas la même taille make_reg"
  in aux (registers,reads,ls overwrite)

let switch inp selec =
  let rec aux i = function
    | [] -> i
    | s::ss -> let n = len i in
      (<;u[n],v[n]>
	  | u = bw_and (dup (not_ s) n) i
	  | v = bw_and (dup s n) i	  
	  | (aux u ss) | (aux v ss)
      )
  in aux inp (List.rev (ls selec))

let make_word_port () =
  create_ports word_size
let is_zero p =
  let rec aux = function
    | [] -> failwith "foreveralone.jpg"
    | [x] -> x
    | t::q -> or_ t (aux q)
  in not_ (aux (ls p))

(* in: V to write in A1 *)
(* out: instr,A2,A3 *)
let proc_core rom v =
  let regcount = 33 in
  let regcount_length = bitsize regcount in
  let register_debug name reg w ow =
    <r[len w]> | r = reg w ow | (output name r)
  in
  let const_reg n w ow =
    const_word n
  in
  let choose_reg = function
    | 0 -> const_reg 0
    | 1 -> incrementing_register
    | n -> register
  in
  let rec registers = function
    | (-1) -> []
    | n -> ((register_debug ("$r"^(string_of_int n)) (choose_reg n)),n)::(registers (n-1)) in
  let registers = List.rev (registers (regcount-1)) in
  let reg_reads = List.map (fun (_,n) -> (make_word_port (), n)) registers in
  let remove_ids1 = List.map fst in
  let remove_ids2 = List.map fst in
  let registers = remove_ids1 registers in
  let reg_from_id id = select reg_reads (extract_range id 0 (regcount_length)) in
  let zero_w = const_word 0 in
  <instr[word_size],r2[word_size],r3[word_size];
  pc[word_size],
  r0[word_size], r0_id[word_size],
  r1[word_size], r1_id[word_size], r1_id_pre[word_size],
  r2_id[word_size],
  r3_id[word_size],
  instr_pre[word_size], reg_overwrite[regcount], reg_a0[word_size],
  ovf,r0_is_0,
  is_imm,instr_type[2],alu_type[word_size - 2 - 1]
  >
  | r0_id,instr_pre,r1_id_pre,r2_id,r3_id = rom pc
  | pc = fst (List.nth reg_reads 1)
  | r0 = reg_from_id r0_id
  | r0_is_0 = is_zero r0
  | instr = select2 zero_w instr_pre r0_is_0
  | r1_id = select2 zero_w r1_id_pre r0_is_0
  | is_imm,instr_type,alu_type = instr
  | r1 = select2 zero_w (reg_from_id r1_id) r0_is_0
  | r2 = select2 zero_w (reg_from_id r2_id) r0_is_0
  | r3 = select2 zero_w (select2 (reg_from_id r3_id) r3_id is_imm) r0_is_0
  | reg_overwrite = extract_range (switch (one ()) (extract_range r1_id 0 regcount_length)) 0 (regcount-1)
  | (make_register_unit registers (remove_ids2 reg_reads) v reg_overwrite)
  | (output "a0" r0)
  | (output "a1" r1)
  | (output "a2" r2)
  | (output "a3" r3)
  | (output "a1_id" r1_id)
  | (output "a2_id" r2_id)
  | (output "a3_id" r3_id)
  | (output "write_mask" reg_overwrite)
  | (output "instr" instr)
  | (output "immediate" is_imm)
(* instr: imm[1] type[2] alu_type[:] *)
(* type : 00 alu 10 load 01 store *)
let const_rom pc =
  <;>
  | (const_word 3) | (const_word 0) | (const_word 1) | (const_word 0) | (const_word 1)

let rec exp x = function
  | 0 -> 1
  | n when n mod 2 = 0 -> exp (x*x) (n/2)
  | n -> x*(exp x (n-1))

let rom name size addr =
  <;write_flag, write_data[word_size*5]> (* unusued *)
  | (ram { ram_size = size; addr_size = word_size; data_size = word_size*5; ram_name = name} addr write_flag write_data)

let proc () =
  let alu_codes = {
    bw_or = 0;
    bw_and = 1;
    bw_xor = 2;
    bw_not = 3;
    add = 4;
    neg = 5;
    sub = 6;
    mul = 7;
    is_pos = 8;
  } in
  <;
  dummy,i_type1,i_type2,alu_type[word_size - 3], a2[word_size], a3[word_size],
  v_alu[word_size], ovf,
  v_ram[word_size],
  v[word_size]
  >
  | dummy,i_type1,i_type2,alu_type,a2,a3 = proc_core (rom "program_rom" 100) v
  | v_alu,ovf = alu a2 a3 alu_type alu_codes
  | v_ram = ram { ram_size = 100; addr_size = word_size; data_size = word_size; ram_name = "main_ram" } a3 i_type2 a2
  | v = select2 v_alu v_ram i_type1

let main () =
  proc ()



let _ =
  let c = main () in
  Printf.printf "Starting generation\n";
  generate c Sys.argv.(2);
  Printf.printf "Finished.\n"

