## Usage
### 1. You can run the tester using command "java -jar Tester.jar <c..>" where <c..> means your values of c. Note that because of a bug in phaver, never test c larget than 0.7, or it will stuck in phaver testing.
### 2. Once finished testing, where will be a file named "result.csv" in this folder, you can open this file using excel, select all the data and choose "insert chart" to draw the figure.
### 3. Never delete or modify any file in this folder, even it's an empty folder.
### 4. Do not open, modify, delete any file when the tester is running, or it may lock the file and cause the test to fail.
### 5. Once finish the test, you will have all your model file in folder "model_dir".
### 6. You can modify or create a new model using SpaceEx model editor using command "java -jar moe.jar", make sure to open the xml file and modify whatever the value of c you set to "%s"(without quotation marks) so that the tester will replace %s to the actually value of c automatically when doing test.
### 7. Once you run a test, you can use "sh clean.sh" to make the running dir clean, your result will automatically goes to backedup_result directory with the name of the time you run this command.
