## 1-800-challenge

### About this Exercise

Given a dictionary file and a phone keypad numbers and letters pattern, list all possible word combinations for a given phone number.


![](https://raw.githubusercontent.com/darciopacifico/1-800-challenge/master/src/main/resources/phoneKeypad.png)

**ITU international standard Phone Keypad (ITU E.161)**

### Design Approach

The solution was based on the Search Trie data structure, that is an efficient implementation of a symbol table for String as keys. This symbol table implementation is able to find the keys (words) in a sub-linear performance. The idea is to explore this ability to find the word combinations for phone numbers.

The whole solution was separated into four components:

**ConsoleClient**: Handles the user interface, translates the console inputs to phone matcher requests. Other layers doesn't know the stdin/stdout details.

**PhoneNumberMatcher**: Core class of the solution. Interact with the Ternary Search Trie internal structure and algorithm to explore all possible word combinations. Implements the word combination rules. When some combination are found, the PhoneNumberMatcher call the Consumer implementation instance.

**Trie Nodes**: Represents the dictionary data, organized in a character level hierarchy structure, that efficiently allows the Matcher to navigate through all possible branch combinations.

**Consumer**: The callback to be called at every word combination. The default implementation immediately prints the word found.

![](https://raw.githubusercontent.com/darciopacifico/1-800-challenge/master/src/main/resources/ClassDiagram.png)



