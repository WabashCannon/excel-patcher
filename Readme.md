Excel Patcher
======

Purpose
------
Excel-patcher is a small application developed to help automate the checking and filling of Microsoft Excel files. Please note that this software is in the Beta testing stage, so if issues arise please let me know.

It was developed for the purpose taking Excel files generated from a database, and checking each cell to find errors in formatting, content, and data type. Due to deadline restrictions at the time of development, this program is not perfectly general and can only check the first sheet of an excel file. In that sheet, it can only check cells in columns that have titles in row 1.

If further functionality is desired, feel free to contact me and I will attempt to implement it, time permitting.

Usage
-----
Usage is easy. Once you have the program running, browse for an input excel file and an output directory. If you already have a format file set up, click the **Check**  button and the program will begin running. The text field at the bottom of the window will begin printing information about the sheet. When you see the message "Done checking file, see the output.xlsx" it has completed.

To set up your format file, use the menu to select **File→Edit format file**. This should open the format file in your operating system's default text editor. For information about setting up a format file see [How to use the format file](link to format file readme)

**[How to use the format file](link to format file readme)**

Installation
------
### Quick and easy
Download the executable jar file [here](linktojar) and make sure you have Java
installed. Run the program on from the terminal or command prompt using 

`java -jar /path/to/the/jarfile/patcher.jar`

### From Source
If you want to do this, you probably already know how, but this is the process I use.

1. Install eclipse and the eclipse plugin EGit if you do not have them already
2. Clone the source code in eclipse using EGit
3. Download the Apache POI library  [here](https://poi.apache.org/download.html). Grab the binary distribution and extract it.
4. In eclipse, modify the project's build path by right clicking the project and clicking **Build Path→Configure Build Path...**
5. Import the following libraries, or their equivalent for your version using the **Add External Jars...** button.
  * poi-3.11-20141221.jar
  * poi-ooxml-3.11-20141221.jar
  * poi-ooxml-schemas-3.11-20141221.jar
  * xmlbeans-2.6.0.jar
6. Now you should be done, and you can test by running it in eclipse. Just export it as an executable jar from eclipse and it will run standalone.

