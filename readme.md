# RPGCoreAPIのセットアップ
RPGCoreAPIを使用するには簡単なセットアップが必要です。<br>

<br><br>
## Mavenの設定

<br>

### gradleの場合
```
repositories {
	maven { url 'https://jitpack.io' }
}
dependencies {
	compileOnly 'com.github.Firstmemory-Network:RPGCoreAPI:3cbc414434'
}
```
<br>

###mavenの場合
```
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
<dependency>
	<groupId>com.github.Firstmemory-Network</groupId>
	<artifactId>RPGCoreAPI</artifactId>
	<version>3cbc414434</version>
</dependency>
```
<br>
このページを参考に設定してください。

<br><br>
## 初期設定
RPGCoreAPIが有効か確認し、有効だった場合API取得、無効だった場合はpluginを無効化してください。
### 例
```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import dev.firstmemory.rpgcore.*;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {
    public API api;

    @Override
    public void onEnable() {
        //RPGCoreAPIが有効か確認
        if(!Bukkit.getPluginManager().isPluginEnabled("RPGCoreAPI")) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        //APIを使用できるように変数に格納
        api = RPGCore.Companion.getRPGCoreAPI();
    }

    public void test(Player player) {
        // プレイヤーの残高を100足す
        api.deposit(player, 100);
    }
}
```
<br><br>
## APIを使用する
API内の関数を呼び出すことでレベル、お金、EXPを操作できます。
<br>
<br>
### API.deposit
プレイヤーの残高を足します。
```
Int deposit(Player player, Int value)
```
### API.withdrawal
プレイヤーの残高を減らします。
```
Int withdrawal(Player player, Int value)
```
### API.getBalance
プレイヤーの残高を取得します。
```
Int getBalance(Player player)
```
### API.addExp
プレイヤーのExpを足します。
```
Int addExp(Player player, Int value)
```
### API.removeExp
プレイヤーのExpを減らします。
```
Int removeExp(Player player, Int value)
```
### API.getLevel
プレイヤーのレベルを取得します。
```
Int getLevel(Player player)
```
### API.getStatusPoint
プレイヤーのスキルポイントを取得します。
```
Int getStatusPoint(Player player)
```
### API.setStatusPoint
プレイヤーのスキルポイントを設定します。
```
Int setStatusPoint(Player player, Int value)
```
### API.setStatusLevel
```
void setStatusLevel(Player player, StatusType type, int level)
```
### API.getStatusLevel
```
int getStatusLevel(Player player, StatusType type)
```
### 一部の関数はここに記載されていないためコメントを参照してください。

<br><br><br>
## イベント関連
### PlayerLevelUpEvent
プレイヤーのレベルが上った際に呼び出されます。<br>
オフラインの場合オンラインになった際にキャッシュされた分をすべて上げます。<br>
<br>
### 一部のイベントはここに記載されていないためdev.firstmemoney.rpgcore.eventsを参照してください。
