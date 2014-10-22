type file;

app (file o) split(file f,int i){
	splitswift filename(f) i stdout=filename(o);
}

app (file o) countwords (file i) {
    wordcount filename(i) stdout=filename(o);
}

app (file o) merge (file fs[]) {
    mergefiles filenames(fs) stdout=filename(o);
}

file inputfile <"../input/small-dataset">;
file inputfiles[];

foreach i in [0:9] {
	file f <single_file_mapper; file=strcat("input/wordcount-input-0",i)>;
	f = split(inputfile,i);
	inputfiles[i] = f;
}

file outputfiles[];

foreach f,i in inputfiles {
	file outputfile <single_file_mapper; file=strcat("output/wordcount-output-",i,".out")>;
	outputfile = countwords(f);
	outputfiles[i] = outputfile;
}

file mergedfile <single_file_mapper; file=strcat("output/wordcount-merge.out")>;
mergedfile = merge(outputfiles);