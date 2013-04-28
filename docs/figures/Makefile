EPSFILES = flowsources.eps flowsinks.eps
PDFFILES = flowsources.pdf flowsinks.pdf
PNGFILES = flowsources.png flowsinks.png

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
