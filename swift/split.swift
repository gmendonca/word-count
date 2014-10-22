type file;

app (file o) split(file f,int i){
	splitswift filename(f) i stdout=filename(o);
}

file inputfile <"../input/small-dataset">;
file outputfiles[];

foreach i in [0:9] {
	file outputfile <single_file_mapper; file=strcat("input/wordcount-input-0",i)>;
	outputfile = split(inputfile,i);
	outputfiles[i] = outputfile;
}