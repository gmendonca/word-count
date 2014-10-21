type file;

app (file o) countwords (file i) {
    wordcount filename(i) stdout=filename(o);
}

app (file o) merge (file fs[]) {
    mergefiles filenames(fs) stdout=filename(o);
}

file inputfiles[] <filesys_mapper;prefix="wordcount-input-">;
file outputfiles[];

foreach f,i in inputfiles {
	file outputfile <single_file_mapper; file=strcat("output/wordcount-output-",i,".out")>;
	outputfile = countwords(f);
	outputfiles[i] = outputfile;

}

file mergedfile <single_file_mapper; file=strcat("output/wordcount-merge.out")>;
mergedfile = merge(outputfiles);
