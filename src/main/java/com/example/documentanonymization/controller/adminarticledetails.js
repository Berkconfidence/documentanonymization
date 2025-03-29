import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import './adminarticledetails.css';
import backIcon from '../../../Assets/backicon.png';
import downloadIcon from '../../../Assets/whitedownloadicon.png';
import sendArticleIcon from '../../../Assets/sendarticleicon.png';


const formatDate = (dateString) => {
    if (!dateString) return '';

    const date = new Date(dateString);

    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');

    return `${day}-${month}-${year} ${hours}:${minutes}`;
};

function AdminArticleDetails() {
    const { articleTrackingNumber } = useParams();
    const [selectedStatus, setSelectedStatus] = useState("Alındı");
    const [selectedReviewerBlank, setSelectedReviewerBlank] = useState("Hakem Seç");
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [isReviewerDropdownOpen, setIsReviewerDropdownOpen] = useState(false);
    const [activeTab, setActiveTab] = useState('active');
    const [article, setArticle] = useState([]);
    const [articleError, setArticleError] = useState(null);
    const [reviewers, setReviewers] = useState([]);
    const [selectedReviewer, setSelectedReviewer] = useState(null);

    useEffect(() => {
        const fetchArticle = async () => {
            try {
                const response = await fetch(`/articles/tracking/${articleTrackingNumber}`);
                if (response.ok) {
                    const data = await response.json();
                    setArticle(data);
                }
            } catch (error) {
                setArticleError("Bir hata oluştu. Lütfen tekrar deneyin.");
            }
        };
        const fetchReviewer = async () => {
            try {
                const response = await fetch(`/reviewers/all`);
                if (response.ok) {
                    const data = await response.json();
                    setReviewers(data);
                }
            } catch (error) {
                setArticleError("Bir hata oluştu: " + error.message);
            }
        };
        fetchReviewer();
        fetchArticle();
    }, [articleTrackingNumber]);

    const assignReviewer = async () => {
        if (!selectedReviewer) {
            alert("Lütfen bir hakem seçin.");
            return;
        }
        try {
            const response = await fetch(`/articles/assignReviewer/${articleTrackingNumber}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(selectedReviewer),
            });
            if (response.ok) {
                const data = await response.json();
                alert("Hakem başarıyla atandı.");
                setArticle(data);
            }

        } catch (error) {
            console.error('Hakem atama hatası:', error);
            alert(`Hakem atama sırasında bir hata oluştu: ${error.message}`);
        }
    };

    if(articleError) {
        return <div>{articleError}</div>;
    }

    const toggleDropdown = () => {
        setIsDropdownOpen(!isDropdownOpen);
    };

    const toggleDropdownReviewer = () => {
        setIsReviewerDropdownOpen(!isReviewerDropdownOpen);
    };

    const selectStatus = (status) => {
        setSelectedStatus(status);
        setIsDropdownOpen(false);
    };

    const selectReviewer = (reviewer) => {
        setSelectedReviewerBlank(reviewer.name);
        setSelectedReviewer(reviewer);
        setIsReviewerDropdownOpen(false);
    };

    const handleTabChange = (tab) => {
        setActiveTab(tab);
    }

    // PDF indirme fonksiyonu
    const downloadPDF = async () => {
        try {
            // PDF dosyasını indirmek için endpoint'e istek gönder
            const response = await fetch(`/articles/download/${articleTrackingNumber}`);

            if (!response.ok) {
                throw new Error('PDF indirme işlemi başarısız oldu');
            }

            // Dosyayı blob olarak al
            const blob = await response.blob();

            // Dosya için URL oluştur
            const url = window.URL.createObjectURL(blob);

            // Geçici download link oluştur
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;

            // Dosya adını ayarla
            const fileName = article.fileName || `${articleTrackingNumber}_makale.pdf`;
            a.download = fileName.endsWith('.pdf') ? fileName : `${fileName}.pdf`;

            // Link'i DOM'a ekle, tıkla ve kaldır
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

        } catch (error) {
            console.error('PDF indirme hatası:', error);
            alert('PDF indirme sırasında bir hata oluştu.');
        }
    };

    // Makale görüntüleme fonksiyonu
    const viewArticle = async () => {
        try {
            // PDF dosyasını görüntülemek için endpoint'e istek gönder
            const response = await fetch(`/articles/download/${articleTrackingNumber}`);

            if (!response.ok) {
                throw new Error('PDF görüntüleme işlemi başarısız oldu');
            }

            // Dosyayı blob olarak al
            const blob = await response.blob();

            // Dosya için URL oluştur
            const url = window.URL.createObjectURL(blob);

            // Yeni sekmede aç
            window.open(url, '_blank');

        } catch (error) {
            console.error('PDF görüntüleme hatası:', error);
            alert('PDF görüntüleme sırasında bir hata oluştu.');
        }
    };

    // Makale görüntüleme fonksiyonu
    const viewAnonimizeArticle = async () => {
        try {
            // PDF dosyasını görüntülemek için endpoint'e istek gönder
            const response = await fetch(`/articles/view/${articleTrackingNumber}`);

            if (!response.ok) {
                throw new Error('PDF görüntüleme işlemi başarısız oldu');
            }

            // Dosyayı blob olarak al
            const blob = await response.blob();

            // Dosya için URL oluştur
            const url = window.URL.createObjectURL(blob);

            // Yeni sekmede aç
            window.open(url, '_blank');

        } catch (error) {
            console.error('PDF görüntüleme hatası:', error);
            alert('PDF görüntüleme sırasında bir hata oluştu.');
        }
    };

    // PDF anonimleştirme fonksiyonu
    const anonimizePDF = async () => {
        try {
            // Öncelikle hata ayıklama için istek hakkında detaylı bilgi alalım
            console.log("Anonimleştirme isteği gönderiliyor: " + articleTrackingNumber);

            const response = await fetch(`/articles/anonimize/${articleTrackingNumber}`, {
                method: 'POST'
            });

            console.log("Yanıt durumu:", response.status);

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Hata detayı:", errorText);
                throw new Error(`Anonimleştirme başarısız: ${response.status} ${errorText}`);
            }

            // Başarılı yanıt
            alert("Makale başarıyla anonimleştirildi.");

            // Makale bilgilerini yeniden yükleyelim
            const refreshResponse = await fetch(`/articles/tracking/${articleTrackingNumber}`);
            if (refreshResponse.ok) {
                const refreshedArticle = await refreshResponse.json();
                setArticle(refreshedArticle);
            }
        } catch (error) {
            console.error('PDF anonimleştirme hatası:', error);
            alert(`Anonimleştirme sırasında bir hata oluştu: ${error.message}`);
        }
    }

    return (
        <div>
            <div className="adminarticledetails-header">
                <div className="adminarticledetails-article-header">
                    <a href="/admin/makaleler">
                        <button>
                            <img src={backIcon} alt="back" className="adminarticledetails-back-icon"/>
                        </button>
                    </a>
                    <span>Makaleler</span>
                </div>
            </div>

            <div className="adminarticledetails-card-header">
                <div className="adminarticledetails-card-header1">
                    <div className="adminarticledetails-card">
                        <span className="adminarticledetails-card-title">{article.fileName}</span>
                        <div className="adminarticledetails-card-content">
                            <span className="adminarticledetails-card-content-title">Makale Numarası</span>
                            <span className="adminarticledetails-card-content-text">{article.trackingNumber}</span>
                            <span className="adminarticledetails-card-content-title">E-posta</span>
                            <span className="adminarticledetails-card-content-text">{article.authorEmail}</span>
                            <span className="adminarticledetails-card-content-title">Gönderim Tarihi</span>
                            <span className="adminarticledetails-card-content-text">
                            {article.submissionDate ? formatDate(article.submissionDate) : ''}
                            </span>
                            <span className="adminarticledetails-card-content-title">Makale Durumu</span>
                            <span className="adminarticledetails-card-content-text">{article.status}</span>
                        </div>
                        <div className="adminarticledetails-card-buttons">
                            <button className="adminarticledetails-card-button1" onClick={downloadPDF}>
                                <img src={downloadIcon} alt="back"/>
                                <span>PDF İndir</span>
                            </button>
                            <button className="adminarticledetails-card-button2">
                                <img src={sendArticleIcon} alt="back"/>
                                <span>Anonimleştir ve Hakeme Gönder</span>
                            </button>
                        </div>
                    </div>
                    <div className='adminarticledetails-inquiry-tablist'>
                        <div className='inquiry-tablist-item'>
                            <div
                                className={`adminarticledetails-inquiry-tab ${activeTab === 'active' ? 'active' : ''}`}
                                onClick={() => handleTabChange('active')}
                            >
                                Hakemler
                            </div>
                            <div
                                className={`adminarticledetails-inquiry-tab ${activeTab === 'evaluated' ? 'active' : ''}`}
                                onClick={() => handleTabChange('evaluated')}
                            >
                                İşlem Geçmişi
                            </div>
                        </div>
                    </div>
                    {activeTab === 'active' ? (
                        <div className="adminarticledetails-card4">
                            <p>Hakem Ataması</p>
                            {/* article.reviewer ? */}
                            <p1>Henüz hakem ataması yapılmamış</p1>
                            <p2>Yeni Hakem Ata</p2>
                            <div className="adminarticledetails-card4-content">
                                <div className="adminarticledetails-custom-dropdownreviewer">
                                    <div
                                        className="adminarticledetails-dropdown-header"
                                        onClick={toggleDropdownReviewer}
                                    >
                                        <span>{selectedReviewerBlank}</span>
                                        <i className="dropdown-icon">{isReviewerDropdownOpen ? '▼' : '▲'}</i>
                                    </div>
                                    {isReviewerDropdownOpen && (
                                        <div className="adminarticledetails-dropdown-content">
                                            {reviewers.map((reviewer, index) => (
                                                <div className="adminarticledetails-dropdown-item" key={index} onClick={() => selectReviewer(reviewer)}>{reviewer.name}</div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                                <button onClick={() => assignReviewer()}>
                                    Hakem Ata
                                </button>
                            </div>
                        </div>
                    ) : (
                        <div className="adminarticledetails-card5">
                            <p>İşlem Geçmişi</p>
                            <div className="adminarticledetails-card5-content">
                                <div className="adminarticledetails-card5-item">
                                    <p1>2023-10-15</p1>
                                    <div className="adminarticledetails-card5-item-content">
                                        <span>Makale sisteme yüklendi</span>
                                        <p1>Berk Güven</p1>
                                    </div>
                                </div>
                                <div className="adminarticledetails-card5-item">
                                    <p1>2023-10-15</p1>
                                    <div className="adminarticledetails-card5-item-content">
                                        <span>Makale editör tarafından incelemeye alındı</span>
                                        <p1>Admin</p1>
                                    </div>
                                </div>

                            </div>
                        </div>
                    )}
                </div>
                <div className="adminarticledetails-card-header2">
                    <div className="adminarticledetails-card2">
                        <span className="adminarticledetails-card-title">Hızlı İşlemler</span>
                        <div className="adminarticledetails-card2-content">
                            <button onClick={() => viewArticle()}>
                                <span>Makaleyi Görüntüle</span>
                            </button>
                            <button onClick={() => downloadPDF()}>
                                <span>PDF İndir</span>
                            </button>
                            <button onClick={() => anonimizePDF()}>
                                <span>Anonimleştir</span>
                            </button>
                            <button onClick={() => viewAnonimizeArticle()}>
                                <span>Anonimleşmiş Makaleyi Görüntüle</span>
                            </button>
                            <button>
                                <span>Değerlendirme Sürecini Başlat</span>
                            </button>
                        </div>
                    </div>
                    <div className="adminarticledetails-card3">
                        <span className="adminarticledetails-card-title">Makale Durumu</span>
                        <div className="adminarticledetails-card3-content">
                            <div className="adminarticledetails-custom-dropdown">
                                <div
                                    className="adminarticledetails-dropdown-header"
                                    onClick={toggleDropdown}
                                >
                                    <span>{selectedStatus}</span>
                                    <i className="dropdown-icon">{isDropdownOpen ? '▼' : '▲'}</i>
                                </div>
                                {isDropdownOpen && (
                                    <div className="adminarticledetails-dropdown-content">
                                        <div className="adminarticledetails-dropdown-item" onClick={() => selectStatus("Alındı")}>Alındı</div>
                                        <div className="adminarticledetails-dropdown-item" onClick={() => selectStatus("Hakem Değerlendirmesinde")}>Hakem Değerlendirmesinde</div>
                                        <div className="adminarticledetails-dropdown-item" onClick={() => selectStatus("Değerlendirme Tamamlandı")}>Değerlendirme Tamamlandı</div>
                                        <div className="adminarticledetails-dropdown-item" onClick={() => selectStatus("Yazara iletildi")}>Yazara iletildi</div>
                                    </div>
                                )}
                            </div>
                            <button className="adminarticledetails-status-button">
                                Durumu Güncelle
                            </button>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    );
}

export default AdminArticleDetails;