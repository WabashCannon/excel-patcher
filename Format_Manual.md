How to use the format file
====================

1. Preface
-------------------------------
Note that any blank line, or a line beginning with a "#" symbol will be ignored.
Therefore you can comment your format file by beginning lines with "#" signs.

An example comment:

```
 # This line would not be read in the format file
```

2. Creating a new column format
---------------------------------------------------
You create a new column specification by writing the header
followed by an opening brace, "{", whatever specifications you would like
to have for that column (gone over below), and finally a closing brace "}"
on a new line

An example column specification:

```
ColumnTitle{
   Specifications go here
}
```


3. Adding specifications to a column format
--------------------------------------------------------

Specifications are how you provide information about a column to the program.
These include things such as the necessary Data Types, if and when the column
requires it be filled, ect.

If these are confusing, look at section 4 for some examples. They really aren't that bad.

Additionally, most specifications are given as a single line within a column's
braces. These lines are of the format:

    keyword: arguments

Here keyword is a special word (list of them is below), and arguments are information you want the program to have about that keyword. A quick example before we get into all the possible keywords. Say you want the column to only have data types of integers then between the column's braces you would have the line

    Type: Integer

The list of all available specification keywords is as follows:

1. Type
2. Required
3. MaxPossibleCharacters
4. Value

In more detail:

### Type
**Usage: "Type: *Data\_Type*"**
 
where Data_Type is one of the following:

* String
* Integer
* Boolean
* Decimal(a,b)
* Enumeration

Here Decimal(a,b) requires a and b to be replaced with integers where a is the
total length of the decimal number, and b is how many decimal places to include.
(i.e. Decimal(8,3) would show a value of 4/3 as 0001.333)

Enumerations are written as
Type: Enumeration "Possible Value 1" "Possible Value 2" "Another possible value"
These are used to define a finite set of possible string values for the cell

Examples:

```
Type: String
Type: Decimal(10, 2)
Type: Enumeration "Enum Value 1" "Enum Value 2" "..." "Enum Value N"
```

### Required
**Usage "Required: *Conditional\_Expression*"**

Where Conditional_Expression is a conditional expression as covered in Appendix A (bottom of document).

### MaxPossibleCharacters
**Usage "MaxPossibleCharacters: *Integer*"**

Where Integer is simply the integer maximum number of allowable characters

Examples:

```
MaxPossibleCharacters: 5
MaxPossibleCharacters: 100
```

### Value
**Usage "Value: *Conditional\_Expression*"**

Where Conditional_Expression is a conditional expression as covered in appendix A (bottom of document)


4. Examples
-------------------

### Example 1

Let's start simply. Say you want a column called "TestHeader" that is always required. For every loan you want it to have a data type of boolean (true/false) and it is  true for every borrower. Then you would add the following:

```
TestHeader{
 	Required: yes
 	Type: bool
 	Value: true
}
```

### Example 2

Say we had some column, InputNumber, that was filled with decimal numbers before running the program. Then we wanted to have another column automatically fill with this number when the TestHeader column is true. Then we would have the following:

```
 # this is a valid comment
SomeNumber{
 	Required: when TestHeader is true
	Type: Decimal(6,2)
	Value: InputNumber
}
```

 
### Example 3

Now, for the most complicated example I can think of. Let's say our new header, "CountSomething" is of data type Enumeration. Say the enumeration is chosen based on a condition of the SomeNumber column. Then we may have the following:

```
CountSomething{
 	Required: when SomeNumber isnot ""
 	Enumeration: "HasLess" "HasOne" "HasTwo" "HasThree" "HasMore"
 	Value: HasLess when SomeNumber < 1;
		HasOne when SomeNumber >= 1 and SomeNumber < 2;
		HasTwo when SomeNumber >= 2 and SomeNumber < 3;
		HasThree when SomeNumber >= 3 and SomeNumber < 4;
		HasMore;
}
```

###Example 4

A self checking column is useful for situations where you want to auto-correct some values. Say your input column has some cells filled with the string "NotCorrect" and you want to switch them to "Correct". You would have the following. Note, value has a second line with ColumnTitle so that when ColumnTitle is not equal to NotCorrect, it fills itself with it's own value (or more simply said, it stays the same)

```
ColumnTitle{
	Required: yes
	Value: "Correct" when ColumnTitle = "NotCorrect";
		ColumnTitle
}
```


5. Final Comments
---------------------------

It is recomended (but not required) that you alphabetize all of your column headers so that you may find them more easily. Moreover, this document will be more legible if you indent or tab each specification line (it just makes it easy to see what is inside of the braces for a column header.

If you need help with conditional expressions see the Appendix at the bottom of this file.

Finally, if you need help, feel free to contact me at aedyer@ncsu.edu

Good Luck!

Appendix A: Conditional Expressions
--------------------------------------------------

### Conditional expressions

All conditional statements are of the form

    [value] when [conditional]

(where you don't actually type the "[" or "]" characters)

Here `[value]` can be a column title, a constant value (written with parenthesis around it i.e. `"ConstantValue"`) or simply a `yes`, `no`, `true`, or `false`

`when` is a keyword used by the code, so always add it second

`[conditional]` is the complicated part - see the next section
### Conditionals

#### Single word conditions

A conditional can be a single value, like [value], that evaluates to a true/false. If it is given as a column title it will evaluate to either [if the column exists] or if the column is a boolean it will evaluate to [value in the column] Again, yes/no and true/false are ok here

#### Three word conditions

The other option for a conditional is to have two words with a comparison word between them. Here the conditional looks like

    [term1] [comparator] [term2]

`[comparator]` can be things like `and`, `or`, `equal`, `notequal`, `>`, `<`, ect. (be sensible here, and not all sensible cases work!)

here `[termi]` can be any of the values that were allowed as `[value]` from the beginning of this appendix.


### Logic

You can make advanced conditionals by logically stringing together smaller conditionals. These are written `[conditional 1] [logical 1] [conditional 2] [logical 2] ... [conditional n-1] [logical n-1] [conditional n]` where `[logical i]` is simply an `and`, `or`, `&&`, `||`.

### Nearly done - multi-value conditional expressions

If you want to specify different values under different conditions, you need only make each conditional expression as above and place them on seperate lines with a ";" character at the end of each line. The small exception is that no ";" goes after the last line ( just as in the one line case there was no ";" ). When multi-line conditionals are evaluated, each line's condition is checked until one of them is true, then that line's value is returned. If no lines are true, a default value of false is returned

The example in section 4.3 shows the multi-line conditional with some lines having multiple conditions





