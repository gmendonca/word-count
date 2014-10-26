type file;

app (file o, file log) countwords (file i, file word_count) {
    bash "wordcount.sh" filename(i) stdout=filename(o) stderr=filename(log);
}

app (file o, file log) merge (file fs[], file merge_file) {
    bash "mergefiles.sh" filenames(fs) stdout=filename(o) stderr=filename(log);
}

file inputfiles[] <filesys_mapper;location="input", prefix="wordcount-input-">;
file outputfiles[];

file word_count <"wordcount.sh">;
file merge_file <"mergefiles.sh">;

foreach f,i in inputfiles {
        file outputfile <single_file_mapper; file=strcat("output/wordcount-output-",i,".out")>;
        file logfile <single_file_mapper; file=strcat("output/wordcount-output-",i,".log")>;
        (outputfile, logfile) = countwords(f,word_count);
        outputfiles[i] = outputfile;
}

file mergedfile <single_file_mapper; file=strcat("output/wordcount-merge.out")>;
file mergedlog <single_file_mapper; file=strcat("output/wordcount-merge.log")>;
(mergedfile,mergedlog) = merge(outputfiles,merge_file);