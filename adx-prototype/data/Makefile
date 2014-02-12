PROTO_COMPILER=`which protoc`
SRC_DIR = .
PROTO_SRC_DIR = .
DEST_DIR = .

PY_DEST_DIR = $(DEST_DIR)/python
JAVA_DEST_DIR = $(DEST_DIR)/java
CPP_DEST_DIR = $(DEST_DIR)/cpp

all: python/adsame_rtb_pb2.py adsame_rtb_java cpp/adsame_rtb.pb.cc

python/adsame_rtb_pb2.py : adsame_rtb.proto
	@echo "[[1;32;40mTarget:'[1;32;40mmake python'[0m]"
	mkdir -p $(PY_DEST_DIR)
	$(PROTO_COMPILER) -I=$(SRC_DIR) --python_out=$(PY_DEST_DIR) adsame_rtb.proto 
	@echo "make all done"

adsame_rtb_java : adsame_rtb.proto
	@echo "[[1;32;40mTarget:'[1;32;40mmake java'[0m]"
	mkdir -p $(JAVA_DEST_DIR)
	$(PROTO_COMPILER) -I=$(SRC_DIR) --java_out=$(JAVA_DEST_DIR) adsame_rtb.proto 
	@echo "make all done"

cpp/adsame_rtb.pb.cc : adsame_rtb.proto
	@echo "[[1;32;40mTarget:'[1;32;40mmake cpp'[0m]"
	mkdir -p $(CPP_DEST_DIR)
	$(PROTO_COMPILER) -I=$(SRC_DIR) --cpp_out=$(CPP_DEST_DIR) adsame_rtb.proto 
	@echo "make all done"

cp2java : adsame_rtb_java
	@echo "cp to java"
	cp -frap $(JAVA_DEST_DIR)/* ../src/main/java/

clean : 
	@echo "[[1;32;40mTarget:'[1;32;40mclean'[0m]"
	rm $(PY_DEST_DIR) -rf
	rm $(JAVA_DEST_DIR) -rf
	rm $(CPP_DEST_DIR) -rf
