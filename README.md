# hedge-detector

This repo does not include necessary library dependencies or training data (download links to be provided soon).



To compile:


mkdir bin

javac -cp src:{path_to_library_dependencies}/* -d bin src/org/ccls/nlp/cbt/Experimenter_fb_4_svmlite.java



To run:


export PATH=$PATH:{absolute_path_to_repo}/svm

java -cp bin:desc:{path_to_library_deps}/* org.ccls.nlp.cbt.Experimenter_fb_4_svmlite

