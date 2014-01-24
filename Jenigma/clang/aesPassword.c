#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <openssl/aes.h>

const char* gPasswd = "5AOCoWvyViND6hMi";
const char* gIv = "4kr7okCy0yEEaQ5m";

void aes_box_encrypt(unsigned char* source_string, unsigned char* des_string)
{
    int iLoop = 0;
    int iLen =0;
    AES_KEY aes;
    unsigned char key[AES_BLOCK_SIZE];
    unsigned char iv[AES_BLOCK_SIZE];
    if(NULL == source_string || NULL == des_string)
    {
        return;
    }

    strcpy((char*)key, gPasswd);

    strcpy((char*)iv, gIv);

    if (AES_set_encrypt_key(key, 128, &aes) < 0) 
    {
        return ;
    }

    int length = strlen(source_string);
    if (length % AES_BLOCK_SIZE != 0)
    {
        length = length + (AES_BLOCK_SIZE - (length % AES_BLOCK_SIZE));
    }

    unsigned char* input_string = calloc(length, sizeof(unsigned char));
    memset(input_string, 0, length);
    strncpy((char*)input_string, source_string, strlen(source_string));


    //iLen = strlen(source_string) + 1;
    //if ( strlen(source_string)>=AES_BLOCK_SIZE || 
    //        (strlen(source_string) + 1) % AES_BLOCK_SIZE == 0) {
    //    iLen = strlen(source_string) + 1;
    //} else {
    //    iLen = ((strlen(source_string) + 1) / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE;
    //}

    //printf("iLen encrypt: %d\n", length + 1);

    AES_cbc_encrypt(input_string, des_string, length + 1, &aes, iv, AES_ENCRYPT);
    memset(des_string+length, 0, strlen(des_string)-length);
    free(input_string);

    //printf("encrypted string...\n");
    //for (int i=0; i<strlen(des_string); ++i) {
    //    printf("%u ", des_string[i]);    
    //}
    //printf("---len: %d\n", strlen(des_string));


}

void aes_box_decrypt(unsigned char* source_string, unsigned char* des_string)
{
    int iLoop = 0;
    int iLen =0;
    AES_KEY aes;
    unsigned char key[AES_BLOCK_SIZE];
    unsigned char iv[AES_BLOCK_SIZE];
    if(NULL == source_string || NULL == des_string)
    {
        return;
    }

    strcpy((char*)key, gPasswd);

    strcpy((char*)iv, gIv);

    if (AES_set_decrypt_key(key, 128, &aes) < 0) 
    {
        return ;
    }

    iLen = strlen(source_string)+1;
    //printf("iLen decrypt: %d\n", iLen);

    //printf("decrypting string...\n");
    //for (int i=0; i<iLen; ++i) {
    //    printf("%u ", source_string[i]);    
    //}
    //printf("\n");

    AES_cbc_encrypt(source_string, des_string, iLen, &aes, iv, AES_DECRYPT);
}

static char encoding_table[] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
                                'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                                'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
                                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                                'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
                                'w', 'x', 'y', 'z', '0', '1', '2', '3',
                                '4', '5', '6', '7', '8', '9', '+', '/'};
static char *decoding_table = NULL;
static int mod_table[] = {0, 2, 1};


char *base64_encode(const unsigned char *data,
                    size_t input_length,
                    size_t *output_length) {

    *output_length = 4 * ((input_length + 2) / 3);

    char *encoded_data = malloc(*output_length);
    if (encoded_data == NULL) return NULL;

    for (int i = 0, j = 0; i < input_length;) {

        uint32_t octet_a = i < input_length ? (unsigned char)data[i++] : 0;
        uint32_t octet_b = i < input_length ? (unsigned char)data[i++] : 0;
        uint32_t octet_c = i < input_length ? (unsigned char)data[i++] : 0;

        uint32_t triple = (octet_a << 0x10) + (octet_b << 0x08) + octet_c;

        encoded_data[j++] = encoding_table[(triple >> 3 * 6) & 0x3F];
        encoded_data[j++] = encoding_table[(triple >> 2 * 6) & 0x3F];
        encoded_data[j++] = encoding_table[(triple >> 1 * 6) & 0x3F];
        encoded_data[j++] = encoding_table[(triple >> 0 * 6) & 0x3F];
    }

    for (int i = 0; i < mod_table[input_length % 3]; i++)
        encoded_data[*output_length - 1 - i] = '=';

    return encoded_data;
}


unsigned char *base64_decode(const char *data,
                             size_t input_length,
                             size_t *output_length) {

    if (decoding_table == NULL) build_decoding_table();

    if (input_length % 4 != 0) return NULL;

    *output_length = input_length / 4 * 3;
    if (data[input_length - 1] == '=') (*output_length)--;
    if (data[input_length - 2] == '=') (*output_length)--;

    unsigned char *decoded_data = malloc(*output_length);
    if (decoded_data == NULL) return NULL;

    for (int i = 0, j = 0; i < input_length;) {

        uint32_t sextet_a = data[i] == '=' ? 0 & i++ : decoding_table[data[i++]];
        uint32_t sextet_b = data[i] == '=' ? 0 & i++ : decoding_table[data[i++]];
        uint32_t sextet_c = data[i] == '=' ? 0 & i++ : decoding_table[data[i++]];
        uint32_t sextet_d = data[i] == '=' ? 0 & i++ : decoding_table[data[i++]];

        uint32_t triple = (sextet_a << 3 * 6)
        + (sextet_b << 2 * 6)
        + (sextet_c << 1 * 6)
        + (sextet_d << 0 * 6);

        if (j < *output_length) decoded_data[j++] = (triple >> 2 * 8) & 0xFF;
        if (j < *output_length) decoded_data[j++] = (triple >> 1 * 8) & 0xFF;
        if (j < *output_length) decoded_data[j++] = (triple >> 0 * 8) & 0xFF;
    }

    return decoded_data;
}


void build_decoding_table() {

    decoding_table = malloc(256);

    for (int i = 0; i < 64; i++)
        decoding_table[(unsigned char) encoding_table[i]] = i;
}


void base64_cleanup() {
    free(decoding_table);
}

void test(int argc, char* argv[]) {
    if (argc != 3) return;
    unsigned char ucIsEncrypt = 3;  
    unsigned char sourceStringTemp[255];
    unsigned char dstStringTemp[255] = {0};

    unsigned char* b64str=NULL;
    size_t b64len=0;

    memset(sourceStringTemp, 0 ,255);

    strcpy((char*)sourceStringTemp, argv[1]);
    ucIsEncrypt = atoi(argv[2]);

    if(AES_ENCRYPT == ucIsEncrypt)
    {
        printf("encrypting...\n");
        aes_box_encrypt(sourceStringTemp,dstStringTemp);
        b64str = base64_encode(dstStringTemp, strlen(dstStringTemp), &b64len);
        if (b64str != NULL)
        {
        printf("%s\n", b64str);
        free(b64str);
        }
        return;
    }
    else if(AES_DECRYPT == ucIsEncrypt)
    {
        printf("decrypting...\n");
        b64str = base64_decode(sourceStringTemp, strlen(sourceStringTemp), &b64len);
        aes_box_decrypt(b64str, dstStringTemp);
        printf("%s\n", dstStringTemp);
        free(b64str);
        return ;
    }

    printf("error parameter");
}

void crossLanguage() {
    const char* plaintext = "this string include 1: UPCASE,2: number ";
    const char* assert_cipher = "fYLXK7XcNYIG4HbSM0b3WxGd2ULjQmZXpXV9iF8HzScmIMaTsGI64+ciXIv4rnwj";
    unsigned char dstCipher[1024] = {0};
    unsigned char reCipher[1024] = {0};
    unsigned char* b64enstr=NULL;
    unsigned char* b64destr=NULL;
    size_t b64len=0;

        printf("%s, %d\n", plaintext, strlen(plaintext));
        
        aes_box_encrypt((unsigned char*)plaintext, dstCipher);
        b64enstr = base64_encode(dstCipher, strlen(dstCipher), &b64len);
        printf("%s, %d\n", assert_cipher, strlen(assert_cipher));
        for (int i=0; i< b64len; i++)
        {
            printf("%c", b64enstr[i]);
        }
        printf("\n");

        memset(dstCipher, 0, 1024);
        b64destr = base64_decode(b64enstr, b64len, &b64len);
        aes_box_decrypt(b64destr, dstCipher);
        printf("%s, %d\n", dstCipher, strlen(dstCipher));

    free(b64enstr);
    free(b64destr);

}

void crossLanguage2() {
    const char* plaintext = "this string include 1: UPCASE,2: number,3:中文 ";
    const char* assert_cipher = "fYLXK7XcNYIG4HbSM0b3WxGd2ULjQmZXpXV9iF8HzSdkdXRI88DFWE30ObY6V4XmMx9geKvCrZje1YmFNA8c/A==";
    unsigned char dstCipher[1024] = {0};
    unsigned char reCipher[1024] = {0};
    unsigned char* b64enstr=NULL;
    unsigned char* b64destr=NULL;
    size_t b64len=0;

        printf("%s, %d\n", plaintext, strlen(plaintext));
        
        aes_box_encrypt((unsigned char*)plaintext, dstCipher);
        b64enstr = base64_encode(dstCipher, strlen(dstCipher), &b64len);
        printf("%s, %d\n", assert_cipher, strlen(assert_cipher));
        for (int i=0; i< b64len; i++)
        {
            printf("%c", b64enstr[i]);
        }
        printf("\n");

        memset(dstCipher, 0, 1024);
        b64destr = base64_decode(b64enstr, b64len, &b64len);
        aes_box_decrypt(b64destr, dstCipher);
        printf("%s, %d\n", dstCipher, strlen(dstCipher));

    free(b64enstr);
    free(b64destr);

}

void test_bas64() {
    const char* plaintext = "this string include 1: UPCASE,2: number ";
    unsigned char* b64enstr=NULL;
    unsigned char* b64destr=NULL;
    size_t b64len=0;
    b64enstr = base64_encode(plaintext, strlen(plaintext), &b64len);
    printf("%s, %d\n", b64enstr, b64len);

    free(b64enstr);
    
}

int main(int argc,char *argv[]) 
{
    build_decoding_table();
    /////////////////////////////////
    test(argc, argv);
    crossLanguage();
    crossLanguage2();
    //test_bas64();


    ////////////////////////////////
    base64_cleanup();
    return 0;
}
