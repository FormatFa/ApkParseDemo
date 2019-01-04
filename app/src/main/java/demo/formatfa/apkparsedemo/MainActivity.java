package demo.formatfa.apkparsedemo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.util.IndentingWriter2;
import org.jf.util.Parser;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getPackageManager();

                try {
                    PackageInfo info = pm.getPackageInfo(getPackageName(),PackageManager.GET_ACTIVITIES);
                    //解析自己的dex
                    new ParseTest().execute(info.applicationInfo.publicSourceDir);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }




            }
        });
    }



    class ParseTest  extends AsyncTask<String ,String ,String>
    {

        @Override
        protected void onPreExecute() {
            textView.setText("test..");

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
           textView.append(values[0]);
           textView.append("\n");
        }

        @Override
        protected String doInBackground(String... strings) {


            try {
                //读取一个dex文件，如果是zip文件就读取classes.dex，自己查看构造函数里的实现
                DexFile dex = new DexFile(strings[0]);

                //获取类列表
                List<ClassDefItem> classes = dex.ClassDefsSection.getItems();

                //类的其他属性可查看源码
                for(ClassDefItem cls:classes)
                {

                 //获取类签名
                    String signature = cls.getClassType().getTypeDescriptor();

                    if(!signature.equals("Ldemo/formatfa/apkparsedemo/MainActivity;"))
                    {
                        continue;
                    }


                    publishProgress("class:"+cls.getItemType().toString());
                    //获取函数列表,又direct和virtual函数两种
                    ClassDataItem.EncodedMethod [] vmethods= cls.getClassData().getVirtualMethods();;
                    for(ClassDataItem.EncodedMethod m:vmethods)
                    {
                        //函数也类似
                        publishProgress("method:"+m.method.methodName);
                        Parser parser = new Parser(m.codeItem);

                        //获取代码，这是解析后的，可以查看下dump函数的代码怎么处理codeItem的
                        IndentingWriter2 writer = new IndentingWriter2();
                        parser.dump(writer);
                        publishProgress("代码:");
                        publishProgress(writer.getString());


                    }
                }



            } catch (IOException e) {
                return e.toString();
            }


            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
