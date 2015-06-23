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

To set up your format file, use the menu to select **File→Edit format file**. This should open the format file in your operating system's default text editor. For information about setting up a format file see [How to use the format file](Format_Manual.md)

**[How to use the format file](Format_Manual.md)**

Installation
------
### Quick and easy
Download and extract the latest release from [the release page](https://github.com/WabashCannon/excel-patcher/releases). Once it is extracted, navigate to the
excel-patcher folder in terminal or command prompt and type

`java -jar /path/to/the/jarfile/patcher.jar`

### From Source
If you plan to do this, you probably know most of the details already. I recomend importing the project into Eclipse using EGit to clone this repository. It should be that easy to get it up an running.

