package osu.seclab.inputscope.stringvsa.main;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;

import brut.androlib.AndrolibException;
import brut.androlib.res.AndrolibResources;
import brut.androlib.res.data.ResID;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.data.value.ResStringValue;
import brut.directory.ExtFile;
import soot.jimple.infoflow.android.axml.ApkHandler;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.android.resources.ARSCFileParser;

public class ApkContext {
	String path = null;
	ApkHandler apkh = null;
	ProcessManifest mfest = null;
	ARSCFileParser afp = null;
	ResPackage[] resps = null;

	private ApkContext(String path) {
		this.path = path;
	}

	public String getAbsolutePath() throws ZipException, IOException, AndrolibException {
		if (apkh == null) {
			init();
		}

		return apkh.getAbsolutePath();
	}

	public String getPackageName() {
		try {
			if (apkh == null) {
				init();
			}

			if (mfest != null)
				return mfest.getPackageName();

		} catch (Exception e) {
		}

		return null;
	}


	public String getIdentifier(String name, String type, String packageName) {
		for (ResPackage resp : resps) {
			try {
				return resp.getType(type).getResSpec(name).getId().id + "";

			} catch (AndrolibException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
		return "-1";
	}

	public void init() throws ZipException, IOException, AndrolibException {
		apkh = new ApkHandler(path);

		afp = new ARSCFileParser();
		afp.parse(apkh.getInputStream("resources.arsc"));
		try {
			mfest = new ProcessManifest(apkh.getInputStream("AndroidManifest.xml"));
		} catch (Exception e) {
		}
		ExtFile apkFile = new ExtFile(new File(path));

		AndrolibResources res = new AndrolibResources();
		ResTable resTab = res.getResTable(apkFile, true);
		resps = res.getResPackagesFromApk(apkFile, resTab, true);

		apkh.close();
	}

	public String findResource(int id) {

		String str = String.format("[XML String:%s]", id);
		try {
			if (apkh == null) {
				init();
			}


			for (ResPackage resp : resps) {
				if (resp.getResSpec(new ResID(id)) != null) {
					str = ((ResStringValue) resp.getResSpec(new ResID(id)).getDefaultResource().getValue()).encodeAsResXmlValue();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	static ApkContext apkcontext = null;

	public static ApkContext getInstance(String path) {
		apkcontext = new ApkContext(path);
		return apkcontext;
	}

	public static ApkContext getInstance() {
		return apkcontext;
	}

}
