#!/bin/bash
echo Generate aap and java file directly from s3 log, LOCAL or AWS CLI
echo sh tools/s3PathToPaymentWiremock.sh full_path_of_s3
echo $PWD
if  [[ $1 == s3:* ]] ;
then
    echo AWS CLI path
    aws s3 cp $1 $PWD/your_s3_log.log
else
    echo Local File path
    cp $1 $PWD/your_s3_log.log
fi
bazel run :convert_s3_to_aap $PWD/your_s3_log.log $PWD/your_aap.aap
python tools/payment-wiremock-refiner.py $PWD/your_aap.aap

