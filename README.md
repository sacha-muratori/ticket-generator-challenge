# Ticket Generator Challenge

A small challenge that involves building a Bingo 90 ticket generator.

### Requirements:

    Generate a strip of 6 tickets
        Tickets are created as strips of 6, because this allows every number from 1 to 90 to appear across all 6 tickets. If they buy a full strip of six it means that players are guaranteed to mark off a number every time a number is called.
    A bingo ticket consists of 9 columns and 3 rows.
    Each ticket row contains five numbers and four blank spaces
    Each ticket column consists of one, two or three numbers and never three blanks.
        The first column contains numbers from 1 to 9 (only nine),
        The second column numbers from 10 to 19 (ten), the third, 20 to 29 and so on up until
        The last column, which contains numbers from 80 to 90 (eleven).
    Numbers in the ticket columns are ordered from top to bottom (ASC).
    There can be no duplicate numbers between 1 and 90 in the strip (since you generate 6 tickets with 15 numbers each)

Please make sure you add unit tests to verify the above conditions and an output to view the strips generated (command line is ok).

Try to also think about the performance aspects of your solution. How long does it take to generate 10k strips? The recommended time is less than 1s (with a lightweight random implementation)
  
  
## Installation
### Pre-requisite
Please install docker in your workspace.

### Running the Ticket Generator
The installation is quite easy.  
You can move into the Dockerfile path within the application repository and build the Ticket Generator image:
```bash
docker build -t lindar/bingo .
```

Lastly, just run the docker command (provided you are in the same folder path as above):
```bash
docker run lindar/bingo
```

And that's it!  
Now you have the Ticket Generator available on localhost on port 8080.


## Implementation
### Idea behind the Ticket Generator
The idea through which I have attacked the problem was looking at the different "variations" (permutations) of each range of numbers.  
The ranges, also called ColumnRange as it spans across all tickets 9 columns, are the group of numbers (1-9, 10-19, 20-29, .., 80-90) which we have 
to add in our tickets.  
By looking at the ranges we can subdivide them per size in 3 different ones:  
```
First - 1 to 9 (Size 9)  
Middle - 10 to 19, 20 to 29, 30 to 39, 40 to 49, 50 to 59, 60 to 69, 70 to 79 (All size 10)  
Last - 80 to 90 (Size 11)  
```

Across 6 tickets these are the possible permutations to include all numbers  
```
First - [1, 1, 1, 2, 2, 2] or [1, 1, 1, 1, 2, 3]  
Middle - [1, 1, 1, 1, 3, 3] or [1, 1, 1, 2, 2, 3] or [1, 1, 2, 2, 2, 2]  
Last - [1, 1, 1, 2, 3, 3] or [1, 1, 2, 2, 2, 3] or [1, 2, 2, 2, 2, 2]  
```

By iterating per columns, the algorithm randomly chooses one of these variations.  
The variation number tells us how many numbers we can add per ticket in this current column.  
Based on this number, equal number of rows are selected randomly and equal number of actual values are put in our ticket.  
By actual values I refer to the real numbers which end in the ticket.

Due to ROWS and COLUMNS rules and constraint, I have provided additional safe-fail enhancements as keeping track and counting each 
rows total number of values (which can't be bigger than 5) and potential exception handling into a restart of the algorithm in case of excessive number of values per row.  
  
## Testing
There is one JUnit Test providing all necessary tests one-click away.  
Currently, performance is around 500-800 ms per 10k strip generation.
  
  
## Notes
Dockerfile can be adjusted to align with any CI/CD pipeline.

