package org.nightlabs.jfire.trade.ui.account.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.ui.composite.XComposite;
import org.nightlabs.base.ui.language.I18nTextEditor;
import org.nightlabs.base.ui.language.I18nTextEditor.EditMode;
import org.nightlabs.base.ui.notification.IDirtyStateManager;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.trade.ui.resource.Messages;
import org.nightlabs.l10n.NumberFormatter;

public class AccountGeneralComposite
extends XComposite
{
	private Account account;

	private Label accountNameLbl;
	private I18nTextEditor accountNameEditor;
	
	private Label ownerLbl;
	private Text ownerText;
	
	private Label anchorTypeIDLbl;
	private Text anchorTypeIDText;
	
//	private Label revenueInAccLbl;
//	private Text revenueInAccText;
//
//	private Label revenueOutAccLbl;
//	private Text revenueOutAccText;
	
	private Label balanceLbl;
	private Text balanceText;
	
	private Label currencyLabel;
	private Text currencyText;
	
	private IDirtyStateManager dirtyStateManager;
	
	public AccountGeneralComposite(Composite parent, int style) {
		this(parent, style, null);
	}

	public AccountGeneralComposite(Composite parent, int style, IDirtyStateManager dirtyStateManager) {
		super(parent, style);
		this.dirtyStateManager = dirtyStateManager;
		createControl();
	}
	
	public void createControl()
	{
		setLayout(new GridLayout(2, false));		
		int textStyle = SWT.READ_ONLY | getBorderStyle();
		
		accountNameLbl = new Label(this, SWT.NONE);
		accountNameLbl.setText(Messages.getString("org.nightlabs.jfire.trade.ui.editor.account.ManualMoneyTransferPage.label.accountName")); //$NON-NLS-1$
		accountNameEditor = new I18nTextEditor(this);
		accountNameEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		accountNameEditor.addModifyListener(nameModifyListener);
		
		ownerLbl = new Label(this, SWT.NONE);
		ownerLbl.setText(Messages.getString("org.nightlabs.jfire.trade.ui.editor.account.ManualMoneyTransferPage.label.owner")); //$NON-NLS-1$
		ownerText = new Text(this, textStyle);
		ownerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		anchorTypeIDLbl = new Label(this, SWT.NONE);
		anchorTypeIDLbl.setText(Messages.getString("org.nightlabs.jfire.trade.ui.editor.account.ManualMoneyTransferPage.label.anchorTypeID")); //$NON-NLS-1$
		anchorTypeIDText = new Text(this, textStyle);
		anchorTypeIDText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
//		revenueInAccLbl = new Label(this, SWT.NONE);
//		revenueInAccLbl.setText(Messages.getString("org.nightlabs.jfire.trade.ui.editor.account.ManualMoneyTransferPage.label.revenueInAcc")); //$NON-NLS-1$
//		revenueInAccText = new Text(this, textStyle);
//		revenueInAccText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//		revenueOutAccLbl = new Label(this, SWT.NONE);
//		revenueOutAccLbl.setText(Messages.getString("org.nightlabs.jfire.trade.ui.editor.account.ManualMoneyTransferPage.label.revenueOutAcc")); //$NON-NLS-1$
//		revenueOutAccText = new Text(this, textStyle);
//		revenueOutAccText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		balanceLbl = new Label(this, SWT.NONE);
		balanceLbl.setText(Messages.getString("org.nightlabs.jfire.trade.ui.editor.account.ManualMoneyTransferPage.label.balance")); //$NON-NLS-1$
		balanceText = new Text(this, textStyle);
		balanceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		currencyLabel = new Label(this, SWT.NONE);
		currencyLabel.setText("Currency");
		currencyText = new Text(this, textStyle);
		currencyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
	}
	
	public void setAccount(Account account)
	{
		this.account = account;
		if (account != null)
		{
			accountNameEditor.setI18nText(account.getName(), EditMode.DIRECT);
			ownerText.setText(account.getOwner() == null ? "" : account.getOwner().getPerson().getDisplayName()); //$NON-NLS-1$
			anchorTypeIDText.setText(account.getAccountType().getName().getText());
//			revenueInAccText.setText(account.getRevenueInAccount() == null ? "" : account.getRevenueInAccount().getName().getText()); //$NON-NLS-1$
//			revenueOutAccText.setText(account.getRevenueOutAccount() == null ? "" : account.getRevenueOutAccount().getName().getText()); //$NON-NLS-1$
			
//			balanceText.setText(Long.toString(account.getBalance()));
			balanceText.setText(NumberFormatter.formatCurrency(account.getBalance(), account.getCurrency()));
			currencyText.setText(account.getCurrency().getCurrencyID());
		}
	}

	public Account getAccount() {
		return account;
	}
	
	private ModifyListener nameModifyListener = new ModifyListener(){
		public void modifyText(ModifyEvent e) {
			if (dirtyStateManager != null)
				dirtyStateManager.markDirty();
		}
	};
}
