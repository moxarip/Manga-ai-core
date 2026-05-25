import fs from 'fs';
import path from 'path';
function write(f,c){ fs.mkdirSync(path.dirname(f),{recursive:true}); fs.writeFileSync(f,c.trim()+'\n'); }

write('app/src/main/res/drawable/ic_launcher_background.xml', `
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#3DDC84" android:pathData="M0,0h108v108h-108z"/>
</vector>`);

write('app/src/main/res/drawable/ic_launcher_foreground.xml', `
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFF" android:pathData="M54,54m-30,0a30,30 0,1 1,60 0a30,30 0,1 1,-60 0"/>
</vector>`);

write('app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml', `
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>`);

write('app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml', `
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>`);
console.log("SUCCESS");
