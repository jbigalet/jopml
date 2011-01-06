all: jar doc

demo: jar

jopcirc:
	@(cd jop && make && cd ..)
	@(cd circuits && make && cd ..)

doc:
	@(cd doc && make && cd ..)

jar: jopcirc
#	@(cd simulator && ant -q jar && cp dist/JOP_Simulator.jar ../Demo && cd ..)
#	@(cd launcher && ant -q jar && cp dist/JOP_EasyExec.jar ../Demo && cd ..)

clean:
	@(cd jop && make clean && cd ..)
	@(cd circuits && make clean && cd ..)
#	@(cd simulator && ant -q clean && cd ..)
	@(cd netlists && rm -f *.nl && cd ..)
	@(cd doc && make clean && cd ..)
#	@(cd launcher && ant -q clean && cd ..)
	@(rm -rf Demo/*.jar)
