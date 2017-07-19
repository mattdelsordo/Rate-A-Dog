package com.mdelsordo.rate_a_dog.model;

import android.content.Context;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.mdelsordo.rate_a_dog.util.Logger;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

/**
 * Created by mdelsord on 7/17/17.
 *
 * Handles sending image to the server and retrieving the list of labels
 */

public class AWSUtil {
    private static String TAG = "AWSUtil";

    private static final float MIN_CONFIDENCE = 80F;
    private static final int MAX_LABELS = 10;

    //accepts image and retuns a ByteBuffer
//    public static ByteBuffer imageToBytes(){
//
//    }

    //accepts bytes and returns a collection of labels for the converted image
    public static Collection<Label> detectLabelsTest(byte[] bytes, Context context) throws AmazonRekognitionException{
        AWSCredentials credentials;
//        try {
//            //!!!!TODO: This will not work I need to figure out how to send the ACTUAL credentials!!!!
//            credentials = new EnvironmentVariableCredentialsProvider().getCredentials();
//        } catch(Exception e) {
//            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
//                    + "Please make sure that your credentials file is at the correct "
//                    + "location (/Users/userid/.aws/credentials), and is in a valid format.", e);
//        }

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                "us-east-1:838d53dc-d050-4e10-8856-0f1c6642c431", // Identity pool ID
                Regions.US_EAST_1 // Region
        );
        credentials = credentialsProvider.getCredentials();

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(buffer))
                .withMaxLabels(MAX_LABELS)
                .withMinConfidence(MIN_CONFIDENCE);

        DetectLabelsResult result = rekognitionClient.detectLabels(request);
        List<Label> labels = result.getLabels();

        System.out.println("Detected labels for image.");
        for (Label label: labels) {
            Logger.i(TAG, label.getName() + ": " + label.getConfidence().toString());
        }

        return labels;
    }

    //searches collection of labels for instances of "dog"
    //TODO: i am 0% confident thats how that works at all
    public static boolean containsDog(Collection<Label> labels){
        for(Label l : labels){
            if(l.getName().equals("dog")) return true;
        }
        return false;
    }
}
