SPARTA README:

Along with this README, the SPARTA team has submitted a compressed directory named SPARTA. This directory contains a VirtualBox installation of Ubuntu with a build of SPARTA and examples using SPARTA.  It also includes all code and dependencies needed to build SPARTA.  Important directories and files and their basic use are outlined here but please consult ~/Documents/sparta/sparta-code/docs/manual.pdf for in depth instructions.

VM Login Info 
Note: Log in should happen automatically but, if you find yourself needing this info, here it is.
User: sparta
Password: ThisIsSparta!   
Machine Name:sparta-VirtualBox

~/Documents/.bashrc : contains environment variables necessary to build and run SPARTA 

~/Documents/sparta  : contains ALL the files/dependencies (besides the .bashrc) necessary to build and run SPARTA including examples and documentation.

~/Documents/sparta/sparta-code/docs/manual.pdf  :  manual.pdf contains the directions needed to build and run sparta.  SPARTA has already been built on this VM but you can still follow these instructions to build it from scratch.  Note, this file contains links to the Checker Framework which is a dependency of SPARTA.  manual.pdf normally needs to be built as per the instructions in ~/Documents/sparta/sparta-code/README but has been built already for your convenience.

~/Documents/sparta/sparta-code : contains the actual sparta code
    ant jar - builds the sparta jar
    ant all-tests - executes all the tests of the sparta-code (not the examples checked with sparta)  
	
~/Document/sparta/sparta-subjects : contains the example programs that have been annotated/checked using SPARTA.
    ant all-tests - will run SPARTA checks on the subjects

    Note: The process applied to sparta-subjects is the same process outlined in the manual.pdf section 3.  You can descend into the individual projects and use the commands outlined in section 3.
    E.g.:
        cd ~/Document/sparta/sparta-subjects/Sky
        ant flowtest
    
    or
    
        cd ~/Document/sparta/sparta-subjects/CameraTest
        ant reqperms
    
Please see
~/Documents/sparta/sparta-code/docs/manual.pdf for more in depth instructions. 