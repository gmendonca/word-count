type messagefile;
type countfile;

app (countfile t) countwords (messagefile f) {
    wc "-w" @filename(f) stdout=@filename(t);
}

messagefile inputfile <"alice.txt">;

countfile c <regexp_mapper;
            source=@inputfile,
            match="(.*)txt",
            transform="\\1count">;

c = countwords(inputfile);