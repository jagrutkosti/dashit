# DashIt

DashIt is an Android application that can be used as a dash cam. But wait! It is not just a simple video recording application. It utilizes the Bitcoin Blockchain to timestamp the recorded video for proving the authenticity and timestamp of the video.

The user needs to put their smartphone on the dash cam holder and turn on the application. The application will continuously record the video unless the user stops the application manually or any collision has been detected. Once the collision has been detected, the relevant part of the video will be extracted and a hash will be generated. This hash will then be sent to the Originstamp server for timestamping. The videos and the hash is made available to the user for future reference and verifying it on the server. This concept can be used by the judicial system or by an insurance company to verify the authenticity of the video evidences submitted by the user.

The main idea is to timestamp the video just after an incident has been recorded. Thus, not giving any chance for the user to modify the video. The concept works based on a SHA-256 hash generated from the video files recorded. This hash will change even if a single ‘bit’ of the data has been changed. Hence, if the user modifies the files later at any point in time, the hash will not match with the hash that was sent to Originstamp server. This restricts the user to modify the video file in any way. The authenticity of the video files can be verified by regenerating the hash and matching it with the one that is already on the Originstamp server along with its timestamp.

##### Credits

[1] B. Gipp, N. Meuschke, and A. Gernandt. Decentralized Trusted Timestamping using the Crypto Currency Bitcoin. In Proceedings of the iConference 2015 (to appear), Newport Beach, CA, USA, Mar. 24 -27, 2015.

##### This project is MIT Licensed. 
