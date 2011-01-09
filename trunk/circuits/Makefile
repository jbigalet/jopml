#CIRCUITS=count full_adder modn minus clk2
CIRCUITS=proc
all: $(CIRCUITS)

clean:
	@rm -f $(CIRCUITS)
	@rm -f *.cmo *.cmi *.cmx *.o

%: %.ml
	ocamlopt -pp "camlp4of ../jop/_build/jop.cmo" -I "../jop/_build" ../jop/_build/jopruntime.cmx $< -o $@
