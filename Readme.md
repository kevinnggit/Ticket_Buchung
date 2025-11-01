# brotundbutter
## before you start
run
bin/configure.sh  
to get config data from hopper to local

or run
bin/configure-work.sh
to create config for local network in docker environment

and run
bin/download-libs.sh
to download necessary java-libraries


## build cycle 
bin/build.sh  
- prepare
- compile 
- assemble 
- deploy 
- check

## to clean build and target
bin/clean.sh  

# to clean build, target, lib and app/WEB-INF/lib
bin/clean-all.sh


## to generate a QR-Code Picture with the encoder Zxing
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
