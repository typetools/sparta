EPSFILES = flowsources.eps flowsinks.eps flowsources_parameterized.eps flowsinks_parameterized.eps
PDFFILES = flowsources.pdf flowsinks.pdf flowsources_parameterized.pdf flowsinks_parameterized.pdf
PNGFILES = flowsources.png flowsinks.png flowsources_parameterized.png flowsinks_parameterized.png

all: ${EPSFILES} ${PDFFILES} ${PNGFILES}

%.eps : %.dia
	dia -t eps $<

%.pdf : %.eps
	epstopdf $<

%.png : %.dia
	dia -t png $<

clean:
	@\rm *.eps
	@\rm *.pdf
	@\rm *.png
