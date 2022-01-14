package parser;

public enum TokenType {
    ID, //ID[a-zA-Z\200-\377_][a-zA-Z\200-\377_0-9] | Quoted string with \" escaping "
    RIGHT_ARROW, //'->'
    OPEN_CURLY, //'{'
    CLOSE_CURLY, //'}'
    OPEN_SQUARE, //'['
    CLOSE_SQUARE, //']'
    SEMICOLON, //';'
    COMMA, //','
    EQUALS, //'='
    EOF, //End of file
    ERROR; //Illegal character
}
