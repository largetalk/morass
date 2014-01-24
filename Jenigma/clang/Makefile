SERVER = AESPassword
TARGET = ${SERVER} 

WORK_DIR = $(shell pwd)
#tsp_root = $(shell pwd)/../../
TARGET_DIR=${WORK_DIR}
#HISISDK_DIR=${WORK_DIR}/../../../../../../

OBJECTS =  aesPassword.o 

CC=gcc

LIB_PATH = -L${WORK_DIR}/dst/lib 

CFLAGS =  -Wall -g -std=c99

LIBS = -lcrypto
	
INCLUDE_PATH = -I${WORK_DIR}/dst/include
				

VPATH = 


.PHNOY: all
all:  ${TARGET} clean_obj
obj:  ${OBJECTS}

${SERVER}: ${OBJECTS} 
	${CC}  -o ${TARGET_DIR}/${SERVER} ${OBJECTS} ${LIB_PATH} ${LIBS} ${CFLAGS}
	
%.o:%.c
	${CC}   ${CFLAGS} -c $< ${INCLUDE_PATH} 


#.PHNOY : clean
clean_obj:
	-rm ${OBJECTS}
clean:
	-rm -rf ${TARGET_DIR}/${SERVER} ${OBJECTS}

