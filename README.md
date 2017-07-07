## 添加github仓库地址
```
allprojects {
    repositories {
        jcenter()
        maven{
            url 'https://raw.githubusercontent.com/supaide/maven-repo/master'
        }
    }
}
```

## plugin上传至github
```
mvn install:install-file -Dfile=network-${tag}.[jar|aar] -DgroupId=org.apache.cordova.plugin -DartifactId=network -Dversion=${tag} -Dpackaging=[jar|aar]
cd ~/.m2/repository
git add -f org/apache/cordova/plugin/network
git commit -a -m 'create network-${tag}'
git push origin master
补充：第一次需要对目录进行git初始化
  git init
  git remote add origin git@github.com:supaide/maven-repo.git

```

## cordova jar打包并上传至github
```
git clone git@github.com:apache/cordova-android.git 
cd ${cordova-android}/framework
git checkout ${tag}
android update project -p .
ant jar
// do plugin upload
```
