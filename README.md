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
??

[//]: # (Please install docker compose within your Docker CLI/Engine.)
[//]: # (https://docs.docker.com/compose/install/linux/)

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

### Testing the Ticket Generator
??

### Notes
Dockerfile can be adjusted to align with any CI/CD pipeline.

