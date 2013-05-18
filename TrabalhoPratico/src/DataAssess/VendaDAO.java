/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataAssess;

import DomainModel.ProdutoVenda;
import DomainModel.Venda;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Iara
 */
public class VendaDAO {

    private BD bd;

    public VendaDAO() {
        bd = new BD();
    }

    public boolean Salvar(Venda obj) {
        try {
            if (obj.getCodvenda() == 0) {
                PreparedStatement comando = bd.getConexao().prepareStatement("insert into vendas(valortotal, data) values(?,?)");
                comando.setDouble(1, obj.getValortotal());
                java.sql.Date dt = new java.sql.Date(obj.getData().getTime());
                comando.setDate(2, dt);
                comando.executeUpdate();
            } else {
                PreparedStatement comando = bd.getConexao().prepareStatement("update vendas set valortotal = ?,data = ? where codvenda = ?");
                comando.setDouble(1, obj.getValortotal());
                java.sql.Date dt = new java.sql.Date(obj.getData().getTime());
                comando.setDate(2, dt);
                comando.setDouble(2, obj.getCodvenda());
                comando.executeUpdate();
            }

            for (ProdutoVenda iv : obj.getProduto()) {
                if (iv.isAtivo()) {
                    if (iv.getCodprodvenda() == 0) {
                        PreparedStatement comando = bd.getConexao().prepareStatement("insert into itensvenda(venda, produto, quantidade) values(?,?,?)");
                        comando.setInt(1, obj.getCodvenda());
                        comando.setInt(2, iv.getProduto().getCodProduto());
                        comando.setInt(3, iv.getQuantidade());
                        comando.executeUpdate();
                    } else {
                        PreparedStatement comando = bd.getConexao().prepareStatement("update itensvenda set produto = ?, quantidade = ? where id = ?");
                        comando.setInt(1, iv.getProduto().getCodProduto());
                        comando.setInt(2, iv.getQuantidade());
                        comando.setInt(3, obj.getCodvenda());
                        comando.executeUpdate();
                    }
                } else {
                    PreparedStatement comando = bd.getConexao().prepareStatement("delete from itensvenda where id = ?");
                    comando.setInt(1, obj.getCodvenda());
                    comando.executeUpdate();
                }
            }

            return true;
        } catch (SQLException ex) {
            Logger.getLogger(VendaDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public Venda Abrir(int id) {
        try {
            Venda venda = new Venda();

            PreparedStatement comando = bd.getConexao().prepareStatement("select * from vendas where id = ?");
            comando.setInt(1, id);
            ResultSet resultado = comando.executeQuery();

            resultado.first();

            venda.setCodvenda(resultado.getInt("id"));
            venda.setData(resultado.getDate("data"));
            venda.setValortotal(resultado.getFloat("valorTotal"));

            carregaItens(id, venda);

            return venda;

        } catch (SQLException ex) {
            Logger.getLogger(VendaDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public boolean Apagar(Venda obj) {
        try {
            PreparedStatement comando2 = bd.getConexao().prepareStatement("delete from itensvenda where venda = ?");
            comando2.setInt(1, obj.getCodvenda());
            comando2.executeUpdate();
            
            PreparedStatement comando = bd.getConexao().prepareStatement("delete from vendas where id = ?");
            comando.setInt(1, obj.getCodvenda());
            comando.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(VendaDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public List<Venda> listarTodos() {
        try {
            PreparedStatement comando = bd.getConexao().prepareStatement("select * from vendas ");
            ResultSet resultado = comando.executeQuery();
            
            List<Venda> vendas = new LinkedList<>();
            while (resultado.next()) {
                
                Venda tmp = new Venda();
                
                tmp.setCodvenda(resultado.getInt("codvenda"));
                tmp.setData(resultado.getDate("data"));
                tmp.setValortotal(resultado.getFloat("valortotal"));
                
                
                carregaItens(tmp.getCodvenda(), tmp);
                
                vendas.add(tmp);
            }
            return vendas;
        } catch (SQLException ex) {
            Logger.getLogger(VendaDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public List<Venda> buscar(Venda filtro) {
        try {
            PreparedStatement comando = bd.getConexao().prepareStatement("select * from vendas where valorTotal = ? or data = ? or id = ? ");
            comando.setDouble(1, filtro.getValortotal());
            java.sql.Date dt = new java.sql.Date(filtro.getData().getTime());
            comando.setDate(2, dt);
            comando.setInt(3, filtro.getCodvenda());
            ResultSet resultado = comando.executeQuery();
            
            List<Venda> vendas = new LinkedList<>();
            while (resultado.next()) {
                
                Venda tmp = new Venda();
                
                tmp.setCodvenda(resultado.getInt("Codigo"));
                tmp.setData(resultado.getDate("Data"));
                tmp.setValortotal(resultado.getFloat("valorTotal"));
       
                carregaItens(tmp.getCodvenda(), tmp);
                
                vendas.add(tmp);
            }
            return vendas;
        } catch (SQLException ex) {
            Logger.getLogger(VendaDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void carregaItens(int id, Venda venda) throws SQLException {
        PreparedStatement comando2 = bd.getConexao().prepareStatement("select * from itensvenda where venda = ?");
        comando2.setInt(1, id);
        ResultSet resultado2 = comando2.executeQuery();

        List<ProdutoVenda> itens = new LinkedList<>();
        ProdutoDAO prod = new ProdutoDAO();
        while (resultado2.next()) {
            ProdutoVenda tmp = new ProdutoVenda();
            tmp.setCodprodvenda(resultado2.getInt("Codigo"));
            tmp.setQuantidade(resultado2.getInt("Quantidade"));
            tmp.setProduto(prod.Abrir(resultado2.getInt("Produto")));
            itens.add(tmp);
        }

        venda.setProduto(itens);
    }
}
