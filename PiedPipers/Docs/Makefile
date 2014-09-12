BASE = report

all : $(BASE).pdf 

# LaTeX

#Once we have images and graphs, put them in data directory and change line
#below to
#$(BASE).pdf : $(BASE).tex *.tex data/*/*.p data/*/*.txt

$(BASE).pdf : $(BASE).tex *.tex 
	pdflatex -shell-escape -halt-on-error $(BASE).tex
	if grep -s Citation $(BASE).log; then \
		bibtex $(BASE); \
		pdflatex -halt-on-error $(BASE).tex; \
		pdflatex -halt-on-error $(BASE).tex; \
	fi

spell:
	aspell check --mode=tex --personal=./LOCALWORDS $(BASE).tex

.PHONY : clean
clean :
	rm -rf *.log *.aux $(BASE).pdf $(PRESENTATION).pdf *.bbl *.blg *.toc *.hs \
	*-tb.exe *.v.exe *.v *.vcd $(BASE).out $(PRESENTATION).out *.nav *.snm *.vrb *.gold *.tbout *.tmp *.diff *.dot
