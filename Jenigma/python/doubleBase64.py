import base64

def doubleBase64():
    a = "this is first string,123"
    a1 = base64.b64encode(a)
    a2 = base64.b64encode("p=" + a1 + "&x=1")
    
    print a
    print a1
    print a2
    
    b1 = base64.b64decode(a2)
    b = base64.b64decode(b1)
    
    print b1
    print b
