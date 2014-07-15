Notes:
	Run using administrator rights for windows,linux and mac
	turn off any firewalls router and os , they mess with the zero-conf 	        of jxta/jxse
	sometimes the os doesn't like the program copying files ,
	there are occasional errors in transfers

Install the jar files in Apache,icepdf,jxse and lib into your
eclipse plugins folder

import the project into eclipse

run the application 

-Lecturer setup 
	-make sure the top button on the gui says "present"
	-enter a service name
	-pick a pdf file to synchronize
	-start the service 
	-open the pdf file using the last button

-Student setup (same machine , firewalls cause a problem with our code)
	-make sure your set to "listen"
	-enter a your name , this is seen in the class peer group 
	 allows the lecturer to get instant numbers and names of people 
         who actually show up to lectures
	-click the start service button

now wait for the files to synchronize across the platforms 
once the student has donwloaded the files , they will automatically open a
pdfreader to read it , this reader has no input keys activated (synced to the lecturers key movements instead)

Functionality of lecturer
	-picks file to distribute with class name
	-key events are synced to pass messages to students 
	-can end classes

Functionality of student
	-links currently to only with one class at a time
	-automatically downloads file to be read from the lecturer
	 no mess with finding where to download it
	-key events are synced to lecturers , so students can't change page
	-can stop listening to a class and has all files from it after          finishing


Modules Designers/Implemetors:
	Simon Clynes : Student/Lecturer GUI and pdf readers
	Yuri Gurin : File system directory handling and file management
	Dan Pinzaru : JXSE networking implementation and zero-configuration
	Evan O'Keeffe : System Integration and file synchronization creator