package ca.mt.wb.runtime.spies.sources;

import java.util.Date;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

public class SourcesViewPart extends ViewPart {
    private ISourceProviderService sps;
    private Composite container;
    private Text textArea;
    
    private ISourceProviderListener sourceListener = new ISourceProviderListener() {
        public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
            StringBuilder sb = new StringBuilder("\n\n");
            sb.append(new Date()).append("\nSourceChange:");
            for(Object key : sourceValuesByName.keySet()) {
                sb.append("\n  '").append(key).append("': '").append(sourceValuesByName.get(key)).append("'");
            }
            appendText(sb.toString());
        }

        public void sourceChanged(int sourcePriority, String sourceName, Object sourceValue) {
            StringBuilder sb = new StringBuilder("\n\n");
            sb.append(new Date()).append("\nSourceChange:");
            sb.append("\n  '").append(sourceName).append("': '").append(sourceValue).append("'");
            appendText(sb.toString());            
        }};

    @Override
    public void createPartControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());
        
        textArea = new Text(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY); 
        
        sps = (ISourceProviderService) getSite().getService(ISourceProviderService.class);
        for(ISourceProvider sp : sps.getSourceProviders()) {
            sp.addSourceProviderListener(sourceListener);
        }
    }

    protected void appendText(final String text) {
        if(!textArea.isDisposed()) {
            textArea.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    textArea.append(text);
                }});
        }
        
    }

    @Override
    public void setFocus() {
        if(container != null && !container.isDisposed()) {
            container.setFocus();
        }
        
    }

    public void dispose() {
        for(ISourceProvider sp : sps.getSourceProviders()) {
            sp.removeSourceProviderListener(sourceListener);
        }
        
    }
}
