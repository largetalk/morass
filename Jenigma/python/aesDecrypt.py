#!/usr/bin/env python
#--coding:utf8--

from Crypto.Cipher import AES
from base64 import b64encode, b64decode

class AesCbcCrypt():
    def __init__(self, key, iv):
        self.key = key
        self.mode = AES.MODE_CBC
        self.iv = iv
        self.padding = '\0'

    def pad(s):
        x = AES.block_size - len(s) % AES.block_size
        return s + (bytes([x]) * x)

    def encrypt(self, text):
        cryptor = AES.new(self.key, self.mode, self.iv)
        length = 16
        count = text.count('')
        if count < length:
            add = (length - count) + 1
            text += (self.padding * add)
        elif count > length:
            add = (length - (count % length)) + 1
            text += (self.padding * add)

        self.ciphertext = cryptor.encrypt(text)
        #for x in self.ciphertext: print ord(x),
        return b64encode(self.ciphertext)

    def decrypt(self, text):
        cryptor = AES.new(self.key, self.mode, self.iv)
        plain_text = cryptor.decrypt(b64decode(text))
        return plain_text.rstrip("\0")

def test():
    key = '1234567890abcdef'
    iv = '1234567890abcdef'
    data = '{"a": "123中文", sss} '
    ec = AesCbcCrypt(key, iv)
    encrpt_data = ec.encrypt(data)
    decrpt_data = ec.decrypt(encrpt_data)
    print data
    print encrpt_data
    print decrpt_data
    assert decrpt_data == data
    print '=============================='

def crossLanguage():
    key = '5AOCoWvyViND6hMi'
    iv = '4kr7okCy0yEEaQ5m'
    data = 'this string include 1: UPCASE,2: number,3:中文 '
    assert_cipher = 'fYLXK7XcNYIG4HbSM0b3WxGd2ULjQmZXpXV9iF8HzSdkdXRI88DFWE30ObY6V4XmMx9geKvCrZje1YmFNA8c/A=='
    ec = AesCbcCrypt(key, iv)
    encrpt_data = ec.encrypt(data)
    decrpt_data = ec.decrypt(encrpt_data)
    print data
    print encrpt_data
    print decrpt_data
    assert decrpt_data == data
    assert encrpt_data == assert_cipher
    print '=============================='

def test_base64():
    data = 'this string include 1: UPCASE,2: number '
    b64 = b64encode(data)
    print b64, len(b64)


if __name__ == '__main__':
    crossLanguage()
    #test_base64()
