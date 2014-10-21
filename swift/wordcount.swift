type file;

app (file o) countwords (file i) {
    wordcount filename(i) stdout=filename(o);
}

file inputfile <"../input/small-dataset">;

file outputfile <"wordcount.out">;

outputfile = countwords(inputfile);