var iAlphanumeric = "Este campo solo puede contener valores numericos y letras. Introduzcalo de nuevo."
var iAlphanumericVB = "La contraseÃ±a debe contener al menos un nÃºmero, una letra minÃºscula, una letra mayÃºscula y alguno de los siguientes caracteres . / % $ @ # *. Por favor, introdÃºzcala de nuevo."
var iDocument = "El valor introducido para 'NIF/NIE' tiene caracteres no permitidos";
var iAlphanumericOrEmail = "Este campo solo puede contenter valores numericos, letras y direcciones de correo electronico. Introduzcalo de nuevo."
var defaultEmptyOK = false
var whitespace = " \t\n\r";


function isLetter(c) {
    return (((c >= "a") && (c <= "z")) || ((c >= "A") && (c <= "Z")) || (c == "Ã±") || (c == "Ã‘"))
}

function isAsciiCharPermited(c) {
    return ((c == ".") || (c == "/")  || (c == "%") || (c == "$") || (c == "@") || (c == "#") || (c == "*"))
}

function isDigit(c) {
    return ((c >= "0") && (c <= "9"))
}

function isLetterOrDigit(c) {
    return (isLetter(c) || isDigit(c))
}

function isEmpty(s) {
    return ((s == null) || (s.length == 0))
}

function isAlphanumericVB(s) {
    var i;
    if (isEmpty(s))
        if (isAlphanumeric.arguments.length == 1)
            return defaultEmptyOK;
        else return (isAlphanumeric.arguments[1] == true);
    for (i = 0; i < s.length; i++) {
        var c = s.charAt(i);
        if (!(isLetter(c) || isDigit(c) || isAsciiCharPermited(c)))
            return false;
    }
    return true;
}

function isAlphanumeric(s) {
    var i;
    if (isEmpty(s))
        if (isAlphanumeric.arguments.length == 1)
            return defaultEmptyOK;
        else return (isAlphanumeric.arguments[1] == true);
    for (i = 0; i < s.length; i++) {
        var c = s.charAt(i);
        if (!(isLetter(c) || isDigit(c)))
            return false;
    }
    return true;
}

function isEmail(s) {
    if (isEmpty(s))
        if (isEmail.arguments.length == 1)
            return defaultEmptyOK;
        else return (isEmail.arguments[1] == true);
    var re = /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/;
    return re.test(s);
}

function checkAlphanumeric(theField, emptyOK) {
    if (emptyOK == "Usuario") {
        theField.value = theField.value.toUpperCase();
    }

    if (checkAlphanumeric.arguments.length == 1) emptyOK = defaultEmptyOK;
    if ((emptyOK == true) && (isEmpty(theField.value))) return true;
    else if ((emptyOK == "Usuario") && !(isEmail(theField.value, false) || isAlphanumeric(theField.value, false)))
        return warnInvalid(theField, iAlphanumericOrEmail);
    else if ((emptyOK != "Usuario") && (!isAlphanumeric(theField.value, false)))
        return warnInvalid(theField, iAlphanumeric);
    else {
        if (emptyOK == "Usuario") {

            var cod_toga = theField.value.substring(0, 2);
            var cod_togaADM = theField.value.substring(0, 3);

            var urlLlamada = document.URL;

            if (urlLlamada.indexOf("triodos.es") == -1) {
                if ((cod_toga == "3E") || (cod_togaADM == "U3E")) {
                    parent.location = "https://www.ruralvia.com/avisos/error_acceso.html";
                    return false;
                }
            } else {
                if ((cod_toga != "3E") && (cod_togaADM != "U3E") && (cod_togaADM != "U02")) {
                    parent.location = "https://banking.triodos.es/avisos/error_acceso.html";
                    return false;
                }
            }
        }
        return true;
    }
}

function checkAlphanumericVB(theField, emptyOK) {
    if (emptyOK == "Usuario") {
        theField.value = theField.value.toUpperCase();
    }

    if (checkAlphanumericVB.arguments.length == 1) emptyOK = defaultEmptyOK;
    if ((emptyOK == true) && (isEmpty(theField.value))) return true;
    else if ((emptyOK == "Usuario") && !(isEmail(theField.value, false) || isAlphanumeric(theField.value, false)))
        return warnInvalid(theField, iAlphanumericOrEmail);
    else if ((emptyOK != "Usuario") && (!isAlphanumericVB(theField.value, false)))
        return warnInvalid(theField, iAlphanumericVB);
    else {
        if (emptyOK == "Usuario") {

            var cod_toga = theField.value.substring(0, 2);
            var cod_togaADM = theField.value.substring(0, 3);

            var urlLlamada = document.URL;

            if (urlLlamada.indexOf("triodos.es") == -1) {
                if ((cod_toga == "3E") || (cod_togaADM == "U3E")) {
                    parent.location = "https://www.ruralvia.com/avisos/error_acceso.html";
                    return false;
                }
            } else {
                if ((cod_toga != "3E") && (cod_togaADM != "U3E") && (cod_togaADM != "U02")) {
                    parent.location = "https://banking.triodos.es/avisos/error_acceso.html";
                    return false;
                }
            }
        }
        return true;
    }
}

function warnInvalid(theField, s) {
    theField.focus()
    theField.select()
    showError(s)
    return false
}

function isWhitespace(s) {
    var i;
    if (isEmpty(s)) return true;
    for (i = 0; i < s.length; i++) {
        var c = s.charAt(i);
        if (whitespace.indexOf(c) == -1) return false;
    }
    return true;
}

function checkDocumento(theField, emptyOK) {
    if (checkDocumento.arguments.length == 1) emptyOK = defaultEmptyOK;
    if ((emptyOK == true) && (isEmpty(theField.value))) return true;
    if (isWhitespace(theField.value))
        return warnInvalid(theField, iDocument);

    var i;
    for (i = 0; i < theField.value.length; i++) {
        var c = theField.value.charAt(i);
        if (!isLetter(c) && !isDigit(c) && c != "." && c != "-" && c != "/")
            return warnInvalid(theField, iDocument);
    }
    return true;
}



function showError(e) {
    alert(e);
}