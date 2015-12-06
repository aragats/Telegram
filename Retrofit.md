PROGUARD

If you are using Proguard in your project add the following lines to your configuration:

-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

Good tutorial: sync, async
http://blog.robinchutaux.com/blog/a-smart-way-to-use-retrofit/

How to upload image
https://futurestud.io/blog/retrofit-2-how-to-upload-files-to-server

Other way to upload file
http://stackoverflow.com/questions/18964288/upload-a-file-through-an-http-form-via-multipartentitybuilder-with-a-progress