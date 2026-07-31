with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

# Remove the unused dangerous permission QUERY_ALL_PACKAGES as per P1
content = content.replace('<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />\n', "")

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
