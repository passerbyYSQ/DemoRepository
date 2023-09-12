package top.ysqorz.lenovo;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/5
 */
public class ImportVaultFolderExecutor {
    public static void main(String[] args) {
        JSONObject folder = ZWTUtils.parseJSONConfig("vault_folder.json");
        String vaultID = folder.getStr("Name"); // 根目录的名称与仓库同名、项目组同名
        createPrjTeam(vaultID);

        Queue<JSONObject> queue = new LinkedList<>();
        queue.offer(folder);
        while (!queue.isEmpty()) {
            JSONObject currentFolder = queue.poll();
            JSONObject parentFolder = currentFolder.getJSONObject("ParentVaultFolder");
            String folderPath;
            if (ObjectUtil.isEmpty(parentFolder)) {
                folderPath = "/";
            } else {
                String parentFolderPath = parentFolder.getStr("FolderPath");
                folderPath = parentFolderPath + (parentFolderPath.endsWith("/") ? "" : "/") + currentFolder.getStr("Name").trim();
            }
            // 创建仓库目录
            createVaultFolder(vaultID, folderPath);
            currentFolder.set("FolderPath", folderPath);

//            // 创建图纸的伪分类
//            JSONObject currentDesignCatalog = createPseudoCatalog(folderPath, "Design");
//            JSONObject parentDesignCatalog = currentFolder.getJSONObject("ParentDesignCatalog");
//            if (ObjectUtil.isNotEmpty(parentDesignCatalog)) {
//                createRelationPsdCtlgSub(parentDesignCatalog, currentDesignCatalog);
//            }
//            // 创建文档的伪分类
//            JSONObject currentDocCatalog = createPseudoCatalog(folderPath, "Document");
//            JSONObject parentDocCatalog = currentFolder.getJSONObject("ParentDocCatalog");
//            if (ObjectUtil.isNotEmpty(parentDocCatalog)) {
//                createRelationPsdCtlgSub(parentDocCatalog, currentDocCatalog);
//            }

            List<JSONObject> children = currentFolder.getBeanList("Children", JSONObject.class);
            if (ObjectUtil.isNotEmpty(children)) {
                for (JSONObject child : children) {
                    child.set("ParentVaultFolder", currentFolder);
//                    child.set("ParentDesignCatalog", currentDesignCatalog);
//                    child.set("ParentDocCatalog", currentDocCatalog);
                    queue.offer(child);
                }
            }
        }
    }

    public static JSONObject createRelationPsdCtlgSub(JSONObject objL, JSONObject objR) {
        try {
            return ZWTUtils.createRelation("rPsdCtlgSub", objL, objR, new JSONObject());
        } catch (Exception ex) {
            System.out.printf("创建rPsdCtlgSub失败，objL：%s，objR：%s，错误信息：%s%n",
                    objL.getStr("Name"), objR.getStr("Name"), ex.getMessage());
            return null;
        }
    }

    public static JSONObject createPseudoCatalog(String name, String catalogFamily) {
        try {
            JSONObject creator = new JSONObject()
                    .set("Name", name)
                    .set("CatalogFamily", catalogFamily)
                    .set("IsRoot", "/".equals(name) ? "+" : "-");
            return ZWTUtils.createObject("PseudoCatalog", creator);
        } catch (Exception ex) {
            System.out.printf("创建PseudoCatalog失败，Name：%s，CatalogFamily：%s，错误信息：%s%n", name, catalogFamily, ex.getMessage());
            return null;
        }
    }

    public static void createPrjTeam(String name) {
        try {
            JSONObject prjCreator = new JSONObject()
                    .set("Name", name)
                    .set("ExistTeamVault", "+")
                    .set("Creator", "system");
            JSONObject params = new JSONObject()
                    .set("clsName", "PrjTeam")
                    .set("prjCreator", prjCreator)
                    .set("vaultCreator", new JSONObject().set("Creator", "system"));
            IPCRequestDTO reqDTO = new IPCRequestDTO("CreatePrjTeam", "clsName", params);
            JSONObject data = ZWTUtils.dispatch(reqDTO);
            System.out.println(JSONUtil.toJsonStr(data));
        } catch (Exception ex) {
            System.out.printf("创建项目组和协作区失败，Name：%s，错误信息：%s%n", name, ex.getMessage());
        }
    }

    public static JSONObject createVaultFolder(String vaultID, String folderPath) {
        try {
            JSONObject creator = new JSONObject()
                    .set("VaultID", vaultID)
                    .set("FolderPathName", folderPath);
            return ZWTUtils.createObject("VaultFolder", creator);
        } catch (Exception ex) {
            System.out.printf("创建VaultFolder失败，VaultID：%s，FolderPathName：%s，错误信息：%s%n", vaultID, folderPath, ex.getMessage());
            return null;
        }
    }
}