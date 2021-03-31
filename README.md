# Distributed room booking system for NTU CZ4013
This is a course project for Nanyang Technological University's 
CZ4013: Distributed Systems. The program simulates a facilities booking
system between a command line interfaced client and a server, using UDP
as the transport protocol.

## Installation
This project was developed on Java SE 8 update 241. To ensure the program runs
smoothly, download the same, or a later, version of JDK from Oracle 
[here](https://www.oracle.com/sg/java/technologies/javase/javase8u211-later-archive-downloads.html).

Clone the repository to your local machine and run the program from there.
> **Note:**
> We used JetBrain's IntelliJ Idea as our IDE. While it should not affect
> the running of the program, it is recommended to use the same IDE to prevent
> any potential errors.

## Execution
### Starting the Service
To simulate the both client and server on the same machine, open at least 
2 terminals (1 for the server, and at least 1 for the client(s)). **Always 
start the server first!**

The server will log the private IP address that it is exposed to once it is successsfully
initialised. When the client starts up, key in this IP address 
when prompted for the server's IP address. The client will then send a connection
request to the server, and will successfully connect with the server if
it receives a response.

### Client Interaction
The client will continuously print the menu of actions that can be taken.
To interact with the program, key in the choice of action.
> Malformed Inputs: The client does not handle all ASCII characters. 
> Only key in integer numbers!

After selecting the service, a subroutine to obtain the user input will 
be executed. Below are the services that require further client interaction.

#### Get Availability
This service will get the availability of a specific facility for some 
specified days.

```textmate
Input: 
- Facility Name: String 
    > e.g. LT1
- Days: Comma-separated Integers 
    > e.g. 0,2,4,5
    
Malformed Inputs:
- Days:
    - Do not add whitespace or a trailing comma in the input:
        > e.g. 0, 2, 4 or 0,1,3,
    - Do not provide alphabets for days, only input Integers
```

#### Create Booking
This service will create a booking for a timeslot in a facility. 
> **Note:** A booking restriction such that you cannot book across different days
> is in place! See "Malformed Input" below for an example

```textmate
Input:
- Facility Name: String
    > e.g. BTC1
- Start Datetime: Pseudo Datetime in the form D/hh/mm, where
    D is an Integer between 0 and 6 inclusive,
    hh is the hour in 24-hour time format,
    mm is the minute between 00 and 59 inclusive
    > e.g. 1/15/31
- End Datetime: Pseudo Datetime in the form D/hh/mm, similar to Start Datetime

Malformed Inputs:
- (Start/End) Datetime:
    - Do not add whitespace or non-numerical characters in the input
        > e.g. 2/ 09 /-10
    - Do not exceed the bounds of the respective fields as mentioned above
        > e.g. 8/25/63
- End Datetime:
    - Do not input End Datetime to be smaller than Start Datetime
        > e.g. Start: 0/10/00, End: 0/08/00
    - Do not input End Datetime to be on a different day as Start Datetime
        > e.g. Start: 1/23/30, End: 2/01/00
```

#### Update Booking
This service will update an existing booking by a given number of minutes.

```textmate
Input:
- Confirmation ID: String
- Offset: Signed Integer

Malformed Inputs:
- Confirmation ID:
    - Do not input any String that is not a confirmation ID returned by the server
        > e.g. "Th1s_15_A_r4nd0M_5tr1ng"
- Offset:
    - Do not include "+" sign for positive integers
        > e.g. +30
    - Do not add whitespace in the input
        > e.g. - 20
    - Do not input non-Integers
    - Do not input numbers that will cause the booking to shift to another day
        e.g. Booking: 1/00/20 to 1/01/00, offset = -40 will cause Booking to become 
        0/23/40 to 1/00/20, which is illegal
```

#### Observe Facility
This facility will allow the current client to observe the facility for 
any updates from other clients, for a given duration in minutes.

```textmate
Input:
- Facility Name: String
- Duration: Unsigned Integer

Malformed Inputs:
- Duration:
    - Do not input negative numbers or non-Integers
        > e.g. -5
```

#### Final Note about Malformed Inputs
While proper error handling is in-place for most types of inputs, some
malformed inputs will break the program. As this project was done to put 
client-server interaction into practice, the assumption when building the program
was that inputs will be proper and not malign.