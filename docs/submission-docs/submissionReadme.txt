SPARTA README:

The SPARTA team has submitted, along with this readme, a compressed directory SPARTA which contains a VirtualBox installation of Ubuntu and a build of SPARTA and examples using SPARTA.  It also includes all code and dependencies needed to build SPARTA.  The layout of the SPARTA in the Ubuntu VM and it's basic use is outlined here but please consult ~/Documents/sparta/sparta-code/docs/manual.pdf for more in depth instructions.

User: sparta
Password: ThisIsSparta!   
Note: Log in should happen automatically and not need a password.

Machine Name:sparta-VirtualBox

~/Documents/.bashrc : contains environment variables necessary to build and run SPARTA 

~/Documents/sparta  : contains ALL the files/dependencies (besides the .bashrc) necessary to build and run SPARTA including example docs.

~/Documents/sparta/sparta-code/docs/manual.pdf  :  manual.pdf contains the directions needed to build and run sparta.  SPARTA is already build but you can still follow these instructions to build it from scratch.  Note, this file contains links to the checker-framework which is a dependency of SPARTA.  Also, manual.pdf normally needs to be built as per the instructions in ~/Documents/sparta/sparta-code/README

~/Documents/sparta/sparta-code : contains the actual sparta code
	ant jar - builds the sparta jar
	and all-tests - executes all the tests.  
	
	Please see ~/Documents/sparta/sparta-code/docs/manual.pdf for more info.  (Note if this file doesn't exist follow the instructions in ~/Documents/sparta/sparta-code/docs/README)
	
~/Document/sparta/sparta-subjects : contains the example programs that have been annotated/checked using SPARTA.
    ant all-tests - will run SPARTA on the subjects
    
Please see
~/Documents/sparta/sparta-code/docs/manual.pdf for more in depth instructions. 