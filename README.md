#  InputScope
**InputScope** is a static analysis tool to automatically uncover hidden behaviors from user-input validation in mobile apps. The key components of **InputScope** are **Taint Analysis** and **String Value Analysis**. With these two components, **InputScope** takes the apk file of an app as input and uncovers both the context of user-input validation and also the comparison content that can be used with identify different types of hidden behaviors with a set of policies.

For more details, please see the following [running example](#jump) and [our paper](https://web.cse.ohio-state.edu/~lin.3021/file/SP20.pdf) (S&P 2020)

# Dependencies
This is an Eclipse project that depends on Flowdroid:

- [flowdroid](https://github.com/secure-software-engineering/FlowDroid)
    - [soot-infoflow-android](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow-android "soot-infoflow-android")
    - [soot-infoflow-cmd](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow-cmd "soot-infoflow-cmd")
    - [soot-infoflow-summaries](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow-summaries "soot-infoflow-summaries")
    - [soot-infoflow](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow "soot-infoflow")

# <span id="jump">Running Example</span>

### target example code from *example/InputScopeExample.apk*

```java
...

public class LoginActivity extends AppCompatActivity {  

    private String successmsg = "Success";
    private String failmsg = "Fail";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        ...
        final EditText masterpwdEditText = findViewById(R.id.masterpwd);
        final Button masterpwdButton = findViewById(R.id.b_masterpwd);
        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences("password", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("pwd", "tryMasterPwd");
        editor.commit();
        ...
        masterpwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                masterpwdActions(masterpwdEditText.getText().toString());
            }
        });
        ...
    }

    private void masterpwdActions(String value){
        SharedPreferences sharePre = getApplicationContext().getSharedPreferences("password", Context.MODE_PRIVATE);
        String fromSP = sharePre.getString("pwd", "non_exist");

        //user-input validation
        if (value.equals(fromSP) || value.equals("test_mp")){
            Toast.makeText(getApplicationContext(), "Master Password Test "+successmsg, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(), "Master Password Test "+failmsg, Toast.LENGTH_LONG).show();
        }
    }  
}
```

### run
Please make sure the taintrules.json file is in the same directory as InputScope.jar
```sh
$ java -jar InputScope.jar ./example/InputScopeExample.apk ./libs/android.jar
May 20, 2019 8:53:14 PM brut.androlib.res.AndrolibResources loadMainPkg
INFO: Loading resource table...
...

{"sinks":[{"unit":"$z0 = virtualinvoke $r1.<java.lang.String: boolean equals(java.lang.Object)>(\"test_mp\")","method":"<com.example.inputscope.LoginActivity: void masterpwdActions(java.lang.String)>","src":["HARDCODED_STR"],"values":[{"0":["test_mp"]}]},{"unit":"$z0 = virtualinvoke $r1.<java.lang.String: boolean equals(java.lang.Object)>($r4)","method":"<com.example.inputscope.LoginActivity: void masterpwdActions(java.lang.String)>","src":["LOCALFILEShare","HARDCODED_STR"],"values":[{"0":["SharedPreferences_GetString->\"pwd\""]}]}],"source":{"unit":"$r4 = virtualinvoke $r3.<android.widget.EditText: android.text.Editable getText()>()","method":"<com.example.inputscope.LoginActivity$2: void onClick(android.view.View)>","unitIndex":4}}

...
```

### example result explanation
```json
{
   "sinks":[
      {
         "unit":"$z0 = virtualinvoke $r1.<java.lang.String: boolean equals(java.lang.Object)>(\"test_mp\")",
         "method":"<com.example.inputscope.LoginActivity: void masterpwdActions(java.lang.String)>",
         "src":[
            "HARDCODED_STR"
         ],
         "values":[
            {
               "0":[
                  "test_mp"
               ]
            }
         ]
      },
      {
         "unit":"$z0 = virtualinvoke $r1.<java.lang.String: boolean equals(java.lang.Object)>($r4)",
         "method":"<com.example.inputscope.LoginActivity: void masterpwdActions(java.lang.String)>",
         "src":[
            "LOCALFILEShare",
            "HARDCODED_STR"
         ],
         "values":[
            {
               "0":[
                  "SharedPreferences_GetString->\"pwd\""
               ]
            }
         ]
      }
   ],
   "source":{
      "unit":"$r4 = virtualinvoke $r3.<android.widget.EditText: android.text.Editable getText()>()",
      "method":"<com.example.inputscope.LoginActivity$2: void onClick(android.view.View)>",
      "unitIndex":4
   }
}
```
**source** includes information about a single user-input, and each **sink** is for one input validation of such user-input.

# Citing

If you create a research work that uses our work, please citing the associated paper:
```
@inproceedings{inputscope:sp20,
  author    = {Qingchuan Zhao, Chaoshun Zuo, Brendan Dolan-Gavitt, Giancarlo Pellegrino, and Zhiqiang Lin} ,
  title     = {Automatic Uncovering of Hidden Behaviors From Input Validation in Mobile Apps},
  booktitle = {Proceedings of the 2020 IEEE Symposium on Security and Privacy},
  address   = {San Francisco, CA},
  month     = {May},
  year      = 2020,
}
```
